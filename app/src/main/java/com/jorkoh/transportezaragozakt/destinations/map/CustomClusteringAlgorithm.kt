package com.jorkoh.transportezaragozakt.destinations.map

/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.algo.Algorithm
import com.google.maps.android.clustering.algo.StaticCluster
import com.google.maps.android.geometry.Bounds
import com.google.maps.android.geometry.Point
import com.google.maps.android.projection.SphericalMercatorProjection
import com.google.maps.android.quadtree.PointQuadTree

import java.util.*
import kotlin.math.pow

/**
 * A simple clustering algorithm with O(nlog n) performance. Resulting clusters are not
 * hierarchical.
 *
 *
 * High level algorithm:<br></br>
 * 1. Iterate over items in the order they were added (candidate clusters).<br></br>
 * 2. Create a cluster with the center of the item. <br></br>
 * 3. Add all items that are within a certain distance to the cluster. <br></br>
 * 4. Move any items out of an existing cluster if they are closer to another cluster. <br></br>
 * 5. Remove those items from the list of candidate clusters.
 *
 *
 * Clusters have the center of the first element (not the centroid of the items within it).
 */
class CustomClusteringAlgorithm<T : ClusterItem> : Algorithm<T> {

    companion object {
        private const val DEFAULT_MAX_DISTANCE_AT_ZOOM = 85
        private val PROJECTION = SphericalMercatorProjection(1.0)
    }

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

    override fun getClusters(zoom: Double): Set<Cluster<T>> {
        val discreteZoom = zoom.toInt()

        val zoomSpecificSpan = DEFAULT_MAX_DISTANCE_AT_ZOOM.toDouble() / 2.2.pow(discreteZoom.toDouble()) / 100.0

        val visitedCandidates = HashSet<QuadItem<T>>()
        val results = HashSet<Cluster<T>>()
        val distanceToCluster = HashMap<QuadItem<T>, Double>()
        val itemToCluster = HashMap<QuadItem<T>, StaticCluster<T>>()

        synchronized(mQuadTree) {
            for (candidate in mItems) {
                if (visitedCandidates.contains(candidate)) {
                    // Candidate is already part of another cluster.
                    continue
                }

                val searchBounds = createBoundsFromSpan(candidate.point, zoomSpecificSpan)
                val clusterItems: Collection<QuadItem<T>>
                clusterItems = mQuadTree.search(searchBounds)
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
            } else other.mClusterItem.position === mClusterItem.position

        }
    }
}