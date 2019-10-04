package com.jorkoh.transportezaragozakt.destinations.map

import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.algo.Algorithm
import com.google.maps.android.clustering.algo.StaticCluster
import com.google.maps.android.geometry.Bounds
import com.google.maps.android.geometry.Point
import com.google.maps.android.projection.SphericalMercatorProjection
import com.google.maps.android.quadtree.PointQuadTree
import com.jorkoh.transportezaragozakt.destinations.map.CustomClusterItem.ClusterItemType.*
import java.util.*
import kotlin.math.pow

class CustomClusteringAlgorithm<T : ClusterItem>(
    private val busFilterEnabled: LiveData<Boolean>,
    private val tramFilterEnabled: LiveData<Boolean>,
    private val ruralFilterEnabled: LiveData<Boolean>
) : Algorithm<T> {

    companion object {
        private const val DEFAULT_MAX_DISTANCE_AT_ZOOM = 140
        private val PROJECTION = SphericalMercatorProjection(1.0)
    }

    override fun setMaxDistanceBetweenClusteredItems(maxDistance: Int) {}

    override fun getMaxDistanceBetweenClusteredItems(): Int = DEFAULT_MAX_DISTANCE_AT_ZOOM

    private val mItems = HashSet<QuadItem<T>>()
    private val mQuadTree = PointQuadTree<QuadItem<T>>(0.0, 1.0, 0.0, 1.0)

    override fun addItem(item: T) {
        val quadItem = QuadItem(item)
        synchronized(mQuadTree) {
            mItems.add(quadItem)
            mQuadTree.add(quadItem)
        }
    }

    override fun addItems(items: Collection<T>) {
        for (item in items) {
            addItem(item)
        }
    }

    override fun clearItems() {
        synchronized(mQuadTree) {
            mItems.clear()
            mQuadTree.clear()
        }
    }

    override fun removeItem(item: T) {
        // QuadItem delegates hashcode() and equals() to its item so removing any QuadItem to that item will remove the item
        val quadItem = QuadItem(item)
        synchronized(mQuadTree) {
            mItems.remove(quadItem)
            mQuadTree.remove(quadItem)
        }
    }

    override fun removeItems(items: Collection<T>) {
        for (item in items) {
            removeItem(item)
        }
    }

    private fun satisfiesMapFilters(candidate: QuadItem<T>) =
        when ((candidate.mClusterItem as CustomClusterItem).type) {
            BUS_NORMAL, BUS_FAVORITE -> busFilterEnabled.value == true
            TRAM_NORMAL, TRAM_FAVORITE -> tramFilterEnabled.value == true
            RURAL_NORMAL, RURAL_FAVORITE, RURAL_TRACKING -> ruralFilterEnabled.value == true
        }

    override fun getClusters(zoom: Double): Set<Cluster<T>> {
        val discreteZoom = zoom.toInt()

        val zoomSpecificSpan = DEFAULT_MAX_DISTANCE_AT_ZOOM.toDouble() / 2.2.pow(discreteZoom.toDouble()) / 100.0

        val visitedCandidates = HashSet<QuadItem<T>>()
        val results = HashSet<Cluster<T>>()
        val distanceToCluster = HashMap<QuadItem<T>, Double>()
        val itemToCluster = HashMap<QuadItem<T>, StaticCluster<T>>()

        synchronized(mQuadTree) {
            val filteredTree = PointQuadTree<QuadItem<T>>(0.0, 1.0, 0.0, 1.0)
            val filteredItems = mItems.filter { satisfiesMapFilters(it) }
            filteredItems.forEach { filteredTree.add(it) }

            for (candidate in filteredItems) {
                if (visitedCandidates.contains(candidate)) {
                    // Candidate is already part of another cluster.
                    continue
                }

                val searchBounds = createBoundsFromSpan(candidate.point, zoomSpecificSpan)
                val clusterItems: Collection<QuadItem<T>> = filteredTree.search(searchBounds)

                if (clusterItems.size == 1) {
                    // Only the current marker is in range. Just add the single item to the results.
                    results.add(candidate)
                    visitedCandidates.add(candidate)
                    distanceToCluster[candidate] = 0.0
                    continue
                }
                val cluster = StaticCluster<T>(candidate.mClusterItem.position)
                results.add(cluster)

                for (clusterItem in clusterItems) {
                    val existingDistance = distanceToCluster[clusterItem]
                    val distance = distanceSquared(clusterItem.point, candidate.point)
                    if (existingDistance != null) {
                        // Item already belongs to another cluster. Check if it's closer to this cluster.
                        if (existingDistance < distance) {
                            continue
                        }
                        // Move item to the closer cluster.
                        itemToCluster[clusterItem]!!.remove(clusterItem.mClusterItem)
                    }
                    distanceToCluster[clusterItem] = distance
                    cluster.add(clusterItem.mClusterItem)
                    itemToCluster[clusterItem] = cluster
                }
                visitedCandidates.addAll(clusterItems)
            }
        }
        return results
    }

    override fun getItems(): Collection<T> {
        val items = ArrayList<T>()
        synchronized(mQuadTree) {
            for (quadItem in mItems) {
                items.add(quadItem.mClusterItem)
            }
        }
        return items
    }

    private fun distanceSquared(a: Point, b: Point): Double {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)
    }

    private fun createBoundsFromSpan(p: Point, span: Double): Bounds {
        val halfSpan = span / 2
        return Bounds(
            p.x - halfSpan, p.x + halfSpan,
            p.y - halfSpan, p.y + halfSpan
        )
    }

    class QuadItem<T : ClusterItem> constructor(val mClusterItem: T) : PointQuadTree.Item, Cluster<T> {
        private val mPoint: Point
        private val mPosition: LatLng = mClusterItem.position
        private val singletonSet: Set<T>

        init {
            mPoint = PROJECTION.toPoint(mPosition)
            singletonSet = setOf(mClusterItem)
        }

        override fun getPoint(): Point {
            return mPoint
        }

        override fun getPosition(): LatLng {
            return mPosition
        }

        override fun getItems(): Set<T> {
            return singletonSet
        }

        override fun getSize(): Int {
            return 1
        }

        override fun hashCode(): Int {
            return mClusterItem.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return if (other !is QuadItem<*>) {
                false
            } else other.mClusterItem.position.latitude == mClusterItem.position.latitude
                    && other.mClusterItem.position.longitude == mClusterItem.position.longitude

        }
    }
}