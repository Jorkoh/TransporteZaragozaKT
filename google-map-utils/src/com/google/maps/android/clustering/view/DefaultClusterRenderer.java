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

package com.google.maps.android.clustering.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.R;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The default view for a ClusterManager. Markers are animated in and out of clusters.
 */
public class DefaultClusterRenderer<T extends ClusterItem> implements ClusterRenderer<T> {
    private final GoogleMap mMap;
    private final IconGenerator mIconGeneratorB;
    private final IconGenerator mIconGeneratorT;
    private final IconGenerator mIconGeneratorR;
    private final IconGenerator mIconGeneratorBT;
    private final IconGenerator mIconGeneratorBR;
    private final IconGenerator mIconGeneratorTR;
    private final IconGenerator mIconGeneratorBTR;
    private final ClusterManager<T> mClusterManager;
    private final float mDensity;
    private boolean mAnimate;

    private static final int[] BUCKETS = {10, 20, 50, 100, 200, 500, 1000};
    private static final int BUS_COLOR = Color.parseColor("#ff2c82c9");
    private static final int TRAM_COLOR = Color.parseColor("#ff232828");
    private static final int RURAL_COLOR = Color.parseColor("#ff409a44");
    private static final int SEPARATOR_COLOR = Color.parseColor("#ffF5F5F5");
    private float[] hsvAccentColor = new float[3];
    private ShapeDrawable mColoredCircleBackgroundB;
    private ShapeDrawable mColoredCircleBackgroundT;
    private ShapeDrawable mColoredCircleBackgroundR;
    private ShapeDrawable mColoredCircleBackgroundBT;
    private ShapeDrawable mColoredCircleBackgroundBR;
    private ShapeDrawable mColoredCircleBackgroundTR;
    private ShapeDrawable mColoredCircleBackgroundBTR;

    /**
     * Markers that are currently on the map.
     */
    private Set<MarkerWithPosition> mMarkers = Collections.newSetFromMap(
            new ConcurrentHashMap<MarkerWithPosition, Boolean>());

    /**
     * Icons for each bucket.
     */
    private SparseArray<BitmapDescriptor> mIconsB = new SparseArray<>();
    private SparseArray<BitmapDescriptor> mIconsT = new SparseArray<>();
    private SparseArray<BitmapDescriptor> mIconsR = new SparseArray<>();
    private SparseArray<BitmapDescriptor> mIconsBT = new SparseArray<>();
    private SparseArray<BitmapDescriptor> mIconsBR = new SparseArray<>();
    private SparseArray<BitmapDescriptor> mIconsTR = new SparseArray<>();
    private SparseArray<BitmapDescriptor> mIconsBTR = new SparseArray<>();

    /**
     * Markers for single ClusterItems.
     */
    private MarkerCache<T> mMarkerCache = new MarkerCache<T>();

    /**
     * If cluster size is less than this size, display individual markers.
     */
    private int mMinClusterSize = 4;

    /**
     * The currently displayed set of clusters.
     */
    private Set<? extends Cluster<T>> mClusters;

    /**
     * Lookup between markers and the associated cluster.
     */
    private Map<Marker, Cluster<T>> mMarkerToCluster = new HashMap<>();
    private Map<Cluster<T>, Marker> mClusterToMarker = new HashMap<>();

    /**
     * The target zoom level for the current set of clusters.
     */
    private float mZoom;

    private final ViewModifier mViewModifier = new ViewModifier();

    private ClusterManager.OnClusterClickListener<T> mClickListener;
    private ClusterManager.OnClusterInfoWindowClickListener<T> mInfoWindowClickListener;
    private ClusterManager.OnClusterItemClickListener<T> mItemClickListener;
    private ClusterManager.OnClusterItemInfoWindowClickListener<T> mItemInfoWindowClickListener;

    public DefaultClusterRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
        mMap = map;
        mAnimate = true;
        mDensity = context.getResources().getDisplayMetrics().density;
        mClusterManager = clusterManager;

        mIconGeneratorB = new IconGenerator(context);
        mIconGeneratorB.setContentView(makeSquareTextView(context));
        mIconGeneratorB.setTextAppearance(R.style.amu_ClusterIcon_TextAppearance);
        mIconGeneratorB.setBackground(makeClusterBackgroundB());

        mIconGeneratorT = new IconGenerator(context);
        mIconGeneratorT.setContentView(makeSquareTextView(context));
        mIconGeneratorT.setTextAppearance(R.style.amu_ClusterIcon_TextAppearance);
        mIconGeneratorT.setBackground(makeClusterBackgroundT());

        mIconGeneratorR = new IconGenerator(context);
        mIconGeneratorR.setContentView(makeSquareTextView(context));
        mIconGeneratorR.setTextAppearance(R.style.amu_ClusterIcon_TextAppearance);
        mIconGeneratorR.setBackground(makeClusterBackgroundR());

        mIconGeneratorBT = new IconGenerator(context);
        mIconGeneratorBT.setContentView(makeSquareTextView(context));
        mIconGeneratorBT.setTextAppearance(R.style.amu_ClusterIcon_TextAppearance);
        mIconGeneratorBT.setBackground(makeClusterBackgroundBT());

        mIconGeneratorBR = new IconGenerator(context);
        mIconGeneratorBR.setContentView(makeSquareTextView(context));
        mIconGeneratorBR.setTextAppearance(R.style.amu_ClusterIcon_TextAppearance);
        mIconGeneratorBR.setBackground(makeClusterBackgroundBR());

        mIconGeneratorTR = new IconGenerator(context);
        mIconGeneratorTR.setContentView(makeSquareTextView(context));
        mIconGeneratorTR.setTextAppearance(R.style.amu_ClusterIcon_TextAppearance);
        mIconGeneratorTR.setBackground(makeClusterBackgroundTR());

        mIconGeneratorBTR = new IconGenerator(context);
        mIconGeneratorBTR.setContentView(makeSquareTextView(context));
        mIconGeneratorBTR.setTextAppearance(R.style.amu_ClusterIcon_TextAppearance);
        mIconGeneratorBTR.setBackground(makeClusterBackgroundBTR());

        /**/
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        Color.colorToHSV(value.data, hsvAccentColor);
        /**/
    }

    @Override
    public void onAdd() {
        mClusterManager.getMarkerCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return mItemClickListener != null && mItemClickListener.onClusterItemClick(mMarkerCache.get(marker));
            }
        });

        mClusterManager.getMarkerCollection().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (mItemInfoWindowClickListener != null) {
                    mItemInfoWindowClickListener.onClusterItemInfoWindowClick(mMarkerCache.get(marker));
                }
            }
        });

        mClusterManager.getClusterMarkerCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return mClickListener != null && mClickListener.onClusterClick(mMarkerToCluster.get(marker));
            }
        });

        mClusterManager.getClusterMarkerCollection().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (mInfoWindowClickListener != null) {
                    mInfoWindowClickListener.onClusterInfoWindowClick(mMarkerToCluster.get(marker));
                }
            }
        });
    }

    @Override
    public void onRemove() {
        mClusterManager.getMarkerCollection().setOnMarkerClickListener(null);
        mClusterManager.getMarkerCollection().setOnInfoWindowClickListener(null);
        mClusterManager.getClusterMarkerCollection().setOnMarkerClickListener(null);
        mClusterManager.getClusterMarkerCollection().setOnInfoWindowClickListener(null);
    }

    //TODO SIMPLIFY THIS LOGIC
    //TODO PASS COLORS PROPERLY?
    private LayerDrawable makeClusterBackgroundB() {
        mColoredCircleBackgroundB = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline = new ShapeDrawable(new OvalShape());
        ShapeDrawable separator = new ShapeDrawable(new OvalShape());
        separator.getPaint().setColor(SEPARATOR_COLOR);
        outline.getPaint().setColor(BUS_COLOR);
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline, separator, mColoredCircleBackgroundB});
        int strokeWidthSeparator = (int) ((mDensity * 6) - 1);
        background.setLayerInset(1, strokeWidthSeparator, strokeWidthSeparator, strokeWidthSeparator, strokeWidthSeparator);
        int strokeWidth = (int) (mDensity * 6);
        background.setLayerInset(2, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

    private LayerDrawable makeClusterBackgroundT() {
        mColoredCircleBackgroundT = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline = new ShapeDrawable(new OvalShape());
        ShapeDrawable separator = new ShapeDrawable(new OvalShape());
        separator.getPaint().setColor(SEPARATOR_COLOR);
        outline.getPaint().setColor(TRAM_COLOR);
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline, separator, mColoredCircleBackgroundT});
        int strokeWidthSeparator = (int) ((mDensity * 6) - 1);
        background.setLayerInset(1, strokeWidthSeparator, strokeWidthSeparator, strokeWidthSeparator, strokeWidthSeparator);
        int strokeWidth = (int) (mDensity * 6);
        background.setLayerInset(2, strokeWidth, strokeWidth, strokeWidth, strokeWidth);

        return background;
    }

    private LayerDrawable makeClusterBackgroundR() {
        mColoredCircleBackgroundR = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline = new ShapeDrawable(new OvalShape());
        ShapeDrawable separator = new ShapeDrawable(new OvalShape());
        separator.getPaint().setColor(SEPARATOR_COLOR);
        outline.getPaint().setColor(RURAL_COLOR);
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline, separator, mColoredCircleBackgroundR});
        int strokeWidthSeparator = (int) ((mDensity * 6) - 1);
        background.setLayerInset(1, strokeWidthSeparator, strokeWidthSeparator, strokeWidthSeparator, strokeWidthSeparator);
        int strokeWidth = (int) (mDensity * 6);
        background.setLayerInset(2, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

    private LayerDrawable makeClusterBackgroundBT() {
        mColoredCircleBackgroundBT = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline1 = new ShapeDrawable(new ArcShape(90, 180));
        outline1.getPaint().setColor(BUS_COLOR);
        ShapeDrawable outline2 = new ShapeDrawable(new ArcShape(270, 180));
        outline2.getPaint().setColor(TRAM_COLOR);
        ShapeDrawable separator = new ShapeDrawable(new OvalShape());
        separator.getPaint().setColor(SEPARATOR_COLOR);
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline1, outline2, separator, mColoredCircleBackgroundBT});
        int strokeWidthSeparator = (int) ((mDensity * 6) - 1);
        background.setLayerInset(2, strokeWidthSeparator, strokeWidthSeparator, strokeWidthSeparator, strokeWidthSeparator);
        int strokeWidth = (int) (mDensity * 6);
        background.setLayerInset(3, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

    private LayerDrawable makeClusterBackgroundBR() {
        mColoredCircleBackgroundBR = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline1 = new ShapeDrawable(new ArcShape(90, 180));
        outline1.getPaint().setColor(BUS_COLOR);
        ShapeDrawable outline2 = new ShapeDrawable(new ArcShape(270, 180));
        outline2.getPaint().setColor(RURAL_COLOR);
        ShapeDrawable separator = new ShapeDrawable(new OvalShape());
        separator.getPaint().setColor(SEPARATOR_COLOR);
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline1, outline2, separator, mColoredCircleBackgroundBR});
        int strokeWidthSeparator = (int) ((mDensity * 6) - 1);
        background.setLayerInset(2, strokeWidthSeparator, strokeWidthSeparator, strokeWidthSeparator, strokeWidthSeparator);
        int strokeWidth = (int) (mDensity * 6);
        background.setLayerInset(3, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

    private LayerDrawable makeClusterBackgroundTR() {
        mColoredCircleBackgroundTR = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline1 = new ShapeDrawable(new ArcShape(90, 180));
        outline1.getPaint().setColor(TRAM_COLOR);
        ShapeDrawable outline2 = new ShapeDrawable(new ArcShape(270, 180));
        outline2.getPaint().setColor(RURAL_COLOR);
        ShapeDrawable separator = new ShapeDrawable(new OvalShape());
        separator.getPaint().setColor(SEPARATOR_COLOR);
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline1, outline2, separator, mColoredCircleBackgroundTR});
        int strokeWidthSeparator = (int) ((mDensity * 6) - 1);
        background.setLayerInset(2, strokeWidthSeparator, strokeWidthSeparator, strokeWidthSeparator, strokeWidthSeparator);
        int strokeWidth = (int) (mDensity * 6);
        background.setLayerInset(3, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

    private LayerDrawable makeClusterBackgroundBTR() {
        mColoredCircleBackgroundBTR = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline1 = new ShapeDrawable(new ArcShape(150, 120));
        outline1.getPaint().setColor(BUS_COLOR);
        ShapeDrawable outline2 = new ShapeDrawable(new ArcShape(30, 120));
        outline2.getPaint().setColor(TRAM_COLOR);
        ShapeDrawable outline3 = new ShapeDrawable(new ArcShape(270, 120));
        outline3.getPaint().setColor(RURAL_COLOR);
        ShapeDrawable separator = new ShapeDrawable(new OvalShape());
        separator.getPaint().setColor(SEPARATOR_COLOR);
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline1, outline2, outline3, separator, mColoredCircleBackgroundBTR});
        int strokeWidthSeparator = (int) ((mDensity * 6) - 1);
        background.setLayerInset(3, strokeWidthSeparator, strokeWidthSeparator, strokeWidthSeparator, strokeWidthSeparator);
        int strokeWidth = (int) (mDensity * 6);
        background.setLayerInset(4, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

    private SquareTextView makeSquareTextView(Context context) {
        SquareTextView squareTextView = new SquareTextView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        squareTextView.setLayoutParams(layoutParams);
        squareTextView.setId(R.id.amu_text);
        int padding = (int) (12 * mDensity);
        squareTextView.setPadding(padding, padding, padding, padding);
        return squareTextView;
    }

    protected int getColor(int clusterSize) {
        // Max value to be considered towards darkening the color
        final float sizeRange = 200f;
        // Range to add to the original color
        final float saturationRange = 0.50f;
        final float valueRange = 0.20f;
        // Normalize values over the size range
        final float size = Math.min(clusterSize, sizeRange);
        // Value between 0 and 1 with logarithmic growth
        final float logSize = (float) Math.log10((size / sizeRange)*100);
        //Make sure to don't go over or under color curve limits
        final float saturation = Math.min(hsvAccentColor[1] + (logSize * saturationRange), 1);
        final float value = Math.max(hsvAccentColor[2] - (logSize * valueRange), 0);
        return Color.HSVToColor(new float[]{hsvAccentColor[0], saturation, value});
    }

    protected String getClusterText(int bucket) {
        if (bucket < BUCKETS[0]) {
            return String.valueOf(bucket);
        }
        return bucket + "+";
    }

    /**
     * Gets the "bucket" for a particular cluster. By default, uses the number of points within the
     * cluster, bucketed to some set points.
     */
    protected int getBucket(Cluster<T> cluster) {
        int size = cluster.getSize();
        if (size <= BUCKETS[0]) {
            return size;
        }
        for (int i = 0; i < BUCKETS.length - 1; i++) {
            if (size < BUCKETS[i + 1]) {
                return BUCKETS[i];
            }
        }
        return BUCKETS[BUCKETS.length - 1];
    }

    public int getMinClusterSize() {
        return mMinClusterSize;
    }

    public void setMinClusterSize(int minClusterSize) {
        mMinClusterSize = minClusterSize;
    }

    /**
     * ViewModifier ensures only one re-rendering of the view occurs at a time, and schedules
     * re-rendering, which is performed by the RenderTask.
     */
    @SuppressLint("HandlerLeak")
    private class ViewModifier extends Handler {
        private static final int RUN_TASK = 0;
        private static final int TASK_FINISHED = 1;
        private boolean mViewModificationInProgress = false;
        private RenderTask mNextClusters = null;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TASK_FINISHED) {
                mViewModificationInProgress = false;
                if (mNextClusters != null) {
                    // Run the task that was queued up.
                    sendEmptyMessage(RUN_TASK);
                }
                return;
            }
            removeMessages(RUN_TASK);

            if (mViewModificationInProgress) {
                // Busy - wait for the callback.
                return;
            }

            if (mNextClusters == null) {
                // Nothing to do.
                return;
            }
            Projection projection = mMap.getProjection();

            RenderTask renderTask;
            synchronized (this) {
                renderTask = mNextClusters;
                mNextClusters = null;
                mViewModificationInProgress = true;
            }

            renderTask.setCallback(new Runnable() {
                @Override
                public void run() {
                    sendEmptyMessage(TASK_FINISHED);
                }
            });
            renderTask.setProjection(projection);
            renderTask.setMapZoom(mMap.getCameraPosition().zoom);
            new Thread(renderTask).start();
        }

        public void queue(Set<? extends Cluster<T>> clusters, Boolean animateTask) {
            synchronized (this) {
                // Overwrite any pending cluster tasks - we don't care about intermediate states.
                mNextClusters = new RenderTask(clusters, animateTask);
            }
            sendEmptyMessage(RUN_TASK);
        }
    }


    protected boolean shouldRenderAsClusterWithAnimation(Cluster<T> cluster) {
        return shouldRenderAsCluster(cluster);
    }

    /**
     * Determine whether the cluster should be rendered as individual markers or a cluster.
     */
    protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
        return cluster.getSize() > mMinClusterSize;
    }

    /**
     * Transforms the current view (represented by DefaultClusterRenderer.mClusters and DefaultClusterRenderer.mZoom) to a
     * new zoom level and set of clusters.
     * <p/>
     * This must be run off the UI thread. Work is coordinated in the RenderTask, then queued up to
     * be executed by a MarkerModifier.
     * <p/>
     * There are three stages for the render:
     * <p/>
     * 1. Markers are added to the map
     * <p/>
     * 2. Markers are animated to their final position
     * <p/>
     * 3. Any old markers are removed from the map
     * <p/>
     * When zooming in, markers are animated out from the nearest existing cluster. When zooming
     * out, existing clusters are animated to the nearest new cluster.
     */
    private class RenderTask implements Runnable {
        final Set<? extends Cluster<T>> clusters;
        private Runnable mCallback;
        private Projection mProjection;
        private SphericalMercatorProjection mSphericalMercatorProjection;
        private float mMapZoom;
        private Boolean animateTask;

        private RenderTask(Set<? extends Cluster<T>> clusters, Boolean animateTask) {
            this.clusters = clusters;
            this.animateTask = animateTask;
        }

        /**
         * A callback to be run when all work has been completed.
         *
         * @param callback
         */
        public void setCallback(Runnable callback) {
            mCallback = callback;
        }

        public void setProjection(Projection projection) {
            this.mProjection = projection;
        }

        public void setMapZoom(float zoom) {
            this.mMapZoom = zoom;
            this.mSphericalMercatorProjection = new SphericalMercatorProjection(256 * Math.pow(2, Math.min(zoom, mZoom)));
        }

        @SuppressLint("NewApi")
        public void run() {
            if (clusters.equals(DefaultClusterRenderer.this.mClusters)) {
                mCallback.run();
                return;
            }

            final MarkerModifier markerModifier = new MarkerModifier();

            final float zoom = mMapZoom;
            final boolean zoomingIn = zoom > mZoom;
            final float zoomDelta = zoom - mZoom;

            final Set<MarkerWithPosition> markersToRemove = mMarkers;
            // Prevent crashes: https://issuetracker.google.com/issues/35827242
            LatLngBounds visibleBounds;
            try {
                visibleBounds = mProjection.getVisibleRegion().latLngBounds;
            } catch (Exception e) {
                e.printStackTrace();
                visibleBounds = LatLngBounds.builder()
                        .include(new LatLng(0, 0))
                        .build();
            }
            // TODO: Add some padding, so that markers can animate in from off-screen.

            // Find all of the existing clusters that are on-screen. These are candidates for
            // markers to animate from.
            List<Point> existingClustersOnScreen = null;
            if (DefaultClusterRenderer.this.mClusters != null && mAnimate && animateTask) {
                existingClustersOnScreen = new ArrayList<Point>();
                for (Cluster<T> c : DefaultClusterRenderer.this.mClusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                        Point point = mSphericalMercatorProjection.toPoint(c.getPosition());
                        existingClustersOnScreen.add(point);
                    }
                }
            }


            // Create the new markers and animate them to their new positions.
            final Set<MarkerWithPosition> newMarkers = Collections.newSetFromMap(
                    new ConcurrentHashMap<MarkerWithPosition, Boolean>());
            for (Cluster<T> c : clusters) {
                boolean onScreen = visibleBounds.contains(c.getPosition());
                if (zoomingIn && onScreen && mAnimate && animateTask) {
                    Point point = mSphericalMercatorProjection.toPoint(c.getPosition());
                    Point closest = findClosestCluster(existingClustersOnScreen, point);
                    if (closest != null) {
                        LatLng animateTo = mSphericalMercatorProjection.toLatLng(closest);
                        markerModifier.add(true, new CreateMarkerTask(c, newMarkers, animateTo));
                    } else {
                        markerModifier.add(true, new CreateMarkerTask(c, newMarkers, null));
                    }
                } else {
                    markerModifier.add(onScreen, new CreateMarkerTask(c, newMarkers, null));
                }
            }

            // Wait for all markers to be added.
            markerModifier.waitUntilFree();

            // Don't remove any markers that were just added. This is basically anything that had
            // a hit in the MarkerCache.
            markersToRemove.removeAll(newMarkers);

            // Find all of the new clusters that were added on-screen. These are candidates for
            // markers to animate from.
            List<Point> newClustersOnScreen = null;
            if (mAnimate) {
                newClustersOnScreen = new ArrayList<Point>();
                for (Cluster<T> c : clusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                        Point p = mSphericalMercatorProjection.toPoint(c.getPosition());
                        newClustersOnScreen.add(p);
                    }
                }
            }

            // Remove the old markers, animating them into clusters if zooming out.
            for (final MarkerWithPosition marker : markersToRemove) {
                boolean onScreen = visibleBounds.contains(marker.position);
                // Don't animate when zooming out more than 3 zoom levels.
                // TODO: drop animation based on speed of device & number of markers to animate.
                if (!zoomingIn && zoomDelta > -3 && onScreen && mAnimate && animateTask) {
                    final Point point = mSphericalMercatorProjection.toPoint(marker.position);
                    final Point closest = findClosestCluster(newClustersOnScreen, point);
                    if (closest != null) {
                        LatLng animateTo = mSphericalMercatorProjection.toLatLng(closest);
                        markerModifier.animateThenRemove(marker, marker.position, animateTo);
                    } else {
                        markerModifier.remove(true, marker.marker);
                    }
                } else {
                    markerModifier.remove(onScreen, marker.marker);
                }
            }

            markerModifier.waitUntilFree();

            mMarkers = newMarkers;
            DefaultClusterRenderer.this.mClusters = clusters;
            mZoom = zoom;

            mCallback.run();
        }
    }

    @Override
    public void onClustersChanged(Set<? extends Cluster<T>> clusters, Boolean animateTask) {
        mViewModifier.queue(clusters, animateTask);
    }

    @Override
    public void setOnClusterClickListener(ClusterManager.OnClusterClickListener<T> listener) {
        mClickListener = listener;
    }

    @Override
    public void setOnClusterInfoWindowClickListener(ClusterManager.OnClusterInfoWindowClickListener<T> listener) {
        mInfoWindowClickListener = listener;
    }

    @Override
    public void setOnClusterItemClickListener(ClusterManager.OnClusterItemClickListener<T> listener) {
        mItemClickListener = listener;
    }

    @Override
    public void setOnClusterItemInfoWindowClickListener(ClusterManager.OnClusterItemInfoWindowClickListener<T> listener) {
        mItemInfoWindowClickListener = listener;
    }

    @Override
    public void setAnimation(boolean animate) {
        mAnimate = animate;
    }

    private static double distanceSquared(Point a, Point b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }

    private Point findClosestCluster(List<Point> markers, Point point) {
        if (markers == null || markers.isEmpty()) return null;

        int maxDistance = mClusterManager.getAlgorithm().getMaxDistanceBetweenClusteredItems();
        double minDistSquared = maxDistance * maxDistance;
        Point closest = null;
        for (Point candidate : markers) {
            double dist = distanceSquared(candidate, point);
            if (dist < minDistSquared) {
                closest = candidate;
                minDistSquared = dist;
            }
        }
        return closest;
    }

    /**
     * Handles all markerWithPosition manipulations on the map. Work (such as adding, removing, or
     * animating a markerWithPosition) is performed while trying not to block the rest of the app's
     * UI.
     */
    @SuppressLint("HandlerLeak")
    private class MarkerModifier extends Handler implements MessageQueue.IdleHandler {
        private static final int BLANK = 0;

        private final Lock lock = new ReentrantLock();
        private final Condition busyCondition = lock.newCondition();

        private Queue<CreateMarkerTask> mCreateMarkerTasks = new LinkedList<CreateMarkerTask>();
        private Queue<CreateMarkerTask> mOnScreenCreateMarkerTasks = new LinkedList<CreateMarkerTask>();
        private Queue<Marker> mRemoveMarkerTasks = new LinkedList<Marker>();
        private Queue<Marker> mOnScreenRemoveMarkerTasks = new LinkedList<Marker>();
        private Queue<AnimationTask> mAnimationTasks = new LinkedList<AnimationTask>();

        /**
         * Whether the idle listener has been added to the UI thread's MessageQueue.
         */
        private boolean mListenerAdded;

        private MarkerModifier() {
            super(Looper.getMainLooper());
        }

        /**
         * Creates markers for a cluster some time in the future.
         *
         * @param priority whether this operation should have priority.
         */
        public void add(boolean priority, CreateMarkerTask c) {
            lock.lock();
            sendEmptyMessage(BLANK);
            if (priority) {
                mOnScreenCreateMarkerTasks.add(c);
            } else {
                mCreateMarkerTasks.add(c);
            }
            lock.unlock();
        }

        /**
         * Removes a markerWithPosition some time in the future.
         *
         * @param priority whether this operation should have priority.
         * @param m        the markerWithPosition to remove.
         */
        public void remove(boolean priority, Marker m) {
            lock.lock();
            sendEmptyMessage(BLANK);
            if (priority) {
                mOnScreenRemoveMarkerTasks.add(m);
            } else {
                mRemoveMarkerTasks.add(m);
            }
            lock.unlock();
        }

        /**
         * Animates a markerWithPosition some time in the future.
         *
         * @param marker the markerWithPosition to animate.
         * @param from   the position to animate from.
         * @param to     the position to animate to.
         */
        public void animate(MarkerWithPosition marker, LatLng from, LatLng to) {
            lock.lock();
            mAnimationTasks.add(new AnimationTask(marker, from, to));
            lock.unlock();
        }

        /**
         * Animates a markerWithPosition some time in the future, and removes it when the animation
         * is complete.
         *
         * @param marker the markerWithPosition to animate.
         * @param from   the position to animate from.
         * @param to     the position to animate to.
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void animateThenRemove(MarkerWithPosition marker, LatLng from, LatLng to) {
            lock.lock();
            AnimationTask animationTask = new AnimationTask(marker, from, to);
            animationTask.removeOnAnimationComplete(mClusterManager.getMarkerManager());
            mAnimationTasks.add(animationTask);
            lock.unlock();
        }

        @Override
        public void handleMessage(Message msg) {
            if (!mListenerAdded) {
                Looper.myQueue().addIdleHandler(this);
                mListenerAdded = true;
            }
            removeMessages(BLANK);

            lock.lock();
            try {

                // Perform up to 10 tasks at once.
                // Consider only performing 10 remove tasks, not adds and animations.
                // Removes are relatively slow and are much better when batched.
                for (int i = 0; i < 10; i++) {
                    performNextTask();
                }

                if (!isBusy()) {
                    mListenerAdded = false;
                    Looper.myQueue().removeIdleHandler(this);
                    // Signal any other threads that are waiting.
                    busyCondition.signalAll();
                } else {
                    // Sometimes the idle queue may not be called - schedule up some work regardless
                    // of whether the UI thread is busy or not.
                    // TODO: try to remove this.
                    sendEmptyMessageDelayed(BLANK, 10);
                }
            } finally {
                lock.unlock();
            }
        }

        /**
         * Perform the next task. Prioritise any on-screen work.
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private void performNextTask() {
            if (!mOnScreenRemoveMarkerTasks.isEmpty()) {
                removeMarker(mOnScreenRemoveMarkerTasks.poll());
            } else if (!mAnimationTasks.isEmpty()) {
                mAnimationTasks.poll().perform();
            } else if (!mOnScreenCreateMarkerTasks.isEmpty()) {
                mOnScreenCreateMarkerTasks.poll().perform(this);
            } else if (!mCreateMarkerTasks.isEmpty()) {
                mCreateMarkerTasks.poll().perform(this);
            } else if (!mRemoveMarkerTasks.isEmpty()) {
                removeMarker(mRemoveMarkerTasks.poll());
            }
        }

        private void removeMarker(Marker m) {
            Cluster<T> cluster = mMarkerToCluster.get(m);
            mClusterToMarker.remove(cluster);
            mMarkerCache.remove(m);
            mMarkerToCluster.remove(m);
            mClusterManager.getMarkerManager().remove(m);
        }

        /**
         * @return true if there is still work to be processed.
         */
        public boolean isBusy() {
            try {
                lock.lock();
                return !(mCreateMarkerTasks.isEmpty() && mOnScreenCreateMarkerTasks.isEmpty() &&
                        mOnScreenRemoveMarkerTasks.isEmpty() && mRemoveMarkerTasks.isEmpty() &&
                        mAnimationTasks.isEmpty()
                );
            } finally {
                lock.unlock();
            }
        }

        /**
         * Blocks the calling thread until all work has been processed.
         */
        public void waitUntilFree() {
            while (isBusy()) {
                // Sometimes the idle queue may not be called - schedule up some work regardless
                // of whether the UI thread is busy or not.
                // TODO: try to remove this.
                sendEmptyMessage(BLANK);
                lock.lock();
                try {
                    if (isBusy()) {
                        busyCondition.await();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
        }

        @Override
        public boolean queueIdle() {
            // When the UI is not busy, schedule some work.
            sendEmptyMessage(BLANK);
            return true;
        }
    }

    /**
     * A cache of markers representing individual ClusterItems.
     */
    private static class MarkerCache<T> {
        private Map<T, Marker> mCache = new HashMap<T, Marker>();
        private Map<Marker, T> mCacheReverse = new HashMap<Marker, T>();

        public Marker get(T item) {
            return mCache.get(item);
        }

        public T get(Marker m) {
            return mCacheReverse.get(m);
        }

        public void put(T item, Marker m) {
            mCache.put(item, m);
            mCacheReverse.put(m, item);
        }

        public void remove(Marker m) {
            T item = mCacheReverse.get(m);
            mCacheReverse.remove(m);
            mCache.remove(item);
        }
    }

    /**
     * Called before the marker for a ClusterItem is added to the map.
     */
    protected void onBeforeClusterItemRendered(T item, MarkerOptions markerOptions) {
    }

    /**
     * Called before the marker for a Cluster is added to the map.
     * The default implementation draws a circle with a rough count of the number of items.
     */
    protected void onBeforeClusterRendered(Cluster<T> cluster, MarkerOptions markerOptions) {
        int bucket = getBucket(cluster);
        boolean hasBus = false;
        boolean hasTram = false;
        boolean hasRural = false;

        for (T item : cluster.getItems()) {
            int typeOrdinal = item.getTypeOrdinal();
            if (typeOrdinal < 2) {
                hasBus = true;
            } else if (typeOrdinal < 4) {
                hasTram = true;
            } else {
                hasRural = true;
            }
            if (hasBus && hasTram && hasRural) {
                break;
            }
        }

        SparseArray<BitmapDescriptor> icons;
        IconGenerator iconGenerator;
        ShapeDrawable circleBackground;
        if (hasBus) {
            if (hasTram) {
                if (hasRural) {
                    icons = mIconsBTR;
                    iconGenerator = mIconGeneratorBTR;
                    circleBackground = mColoredCircleBackgroundBTR;
                } else {
                    icons = mIconsBT;
                    iconGenerator = mIconGeneratorBT;
                    circleBackground = mColoredCircleBackgroundBT;
                }
            } else if (hasRural) {
                icons = mIconsBR;
                iconGenerator = mIconGeneratorBR;
                circleBackground = mColoredCircleBackgroundBR;
            } else {
                icons = mIconsB;
                iconGenerator = mIconGeneratorB;
                circleBackground = mColoredCircleBackgroundB;
            }
        } else if (hasTram) {
            if (hasRural) {
                icons = mIconsTR;
                iconGenerator = mIconGeneratorTR;
                circleBackground = mColoredCircleBackgroundTR;
            } else {
                icons = mIconsT;
                iconGenerator = mIconGeneratorT;
                circleBackground = mColoredCircleBackgroundT;
            }
        } else if (hasRural) {
            icons = mIconsR;
            iconGenerator = mIconGeneratorR;
            circleBackground = mColoredCircleBackgroundR;
        } else {
            return;
        }

        BitmapDescriptor descriptor = icons.get(bucket);
        if (descriptor == null) {
            circleBackground.getPaint().setColor(getColor(bucket));
            descriptor = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(getClusterText(bucket)));
            icons.put(bucket, descriptor);
        }
        markerOptions.icon(descriptor);
    }


    /**
     * Called after the marker for a Cluster has been added to the map.
     */
    protected void onClusterRendered(Cluster<T> cluster, Marker marker) {
    }

    /**
     * Called after the marker for a ClusterItem has been added to the map.
     */
    protected void onClusterItemRendered(T clusterItem, Marker marker) {
    }

    /**
     * Get the marker from a ClusterItem
     *
     * @param clusterItem ClusterItem which you will obtain its marker
     * @return a marker from a ClusterItem or null if it does not exists
     */
    public Marker getMarker(T clusterItem) {
        return mMarkerCache.get(clusterItem);
    }

    /**
     * Get the ClusterItem from a marker
     *
     * @param marker which you will obtain its ClusterItem
     * @return a ClusterItem from a marker or null if it does not exists
     */
    public T getClusterItem(Marker marker) {
        return mMarkerCache.get(marker);
    }

    /**
     * Get the marker from a Cluster
     *
     * @param cluster which you will obtain its marker
     * @return a marker from a cluster or null if it does not exists
     */
    public Marker getMarker(Cluster<T> cluster) {
        return mClusterToMarker.get(cluster);
    }

    /**
     * Get the Cluster from a marker
     *
     * @param marker which you will obtain its Cluster
     * @return a Cluster from a marker or null if it does not exists
     */
    public Cluster<T> getCluster(Marker marker) {
        return mMarkerToCluster.get(marker);
    }

    /**
     * Creates markerWithPosition(s) for a particular cluster, animating it if necessary.
     */
    private class CreateMarkerTask {
        private final Cluster<T> cluster;
        private final Set<MarkerWithPosition> newMarkers;
        private final LatLng animateFrom;

        /**
         * @param c            the cluster to render.
         * @param markersAdded a collection of markers to append any created markers.
         * @param animateFrom  the location to animate the markerWithPosition from, or null if no
         *                     animation is required.
         */
        public CreateMarkerTask(Cluster<T> c, Set<MarkerWithPosition> markersAdded, LatLng animateFrom) {
            this.cluster = c;
            this.newMarkers = markersAdded;
            this.animateFrom = animateFrom;
        }

        private void perform(MarkerModifier markerModifier) {
            // This declusters into markers and uses zoom
            if (!shouldRenderAsClusterWithAnimation(cluster)) {
                for (T item : cluster.getItems()) {
                    Marker marker = mMarkerCache.get(item);
                    MarkerWithPosition markerWithPosition;
                    if (marker == null) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        if (animateFrom != null) {
                            markerOptions.position(animateFrom);
                        } else {
                            markerOptions.position(item.getPosition());
                        }
                        if (!(item.getTitle() == null) && !(item.getSnippet() == null)) {
                            markerOptions.title(item.getTitle());
                            markerOptions.snippet(item.getSnippet());
                        } else if (!(item.getSnippet() == null)) {
                            markerOptions.title(item.getSnippet());
                        } else if (!(item.getTitle() == null)) {
                            markerOptions.title(item.getTitle());
                        }
                        onBeforeClusterItemRendered(item, markerOptions);
                        marker = mClusterManager.getMarkerCollection().addMarker(markerOptions);
                        markerWithPosition = new MarkerWithPosition(marker);
                        mMarkerCache.put(item, marker);
                        if (animateFrom != null) {
                            markerModifier.animate(markerWithPosition, animateFrom, item.getPosition());
                        }
                    } else {
                        markerWithPosition = new MarkerWithPosition(marker);
                    }
                    onClusterItemRendered(item, marker);
                    newMarkers.add(markerWithPosition);
                }
                return;
            }

            //This declusters into clusters
            Marker marker = mClusterToMarker.get(cluster);
            MarkerWithPosition markerWithPosition;
            if (marker == null) {
                MarkerOptions markerOptions = new MarkerOptions().
                        position(animateFrom == null ? cluster.getPosition() : animateFrom);
                onBeforeClusterRendered(cluster, markerOptions);
                marker = mClusterManager.getClusterMarkerCollection().addMarker(markerOptions);
                mMarkerToCluster.put(marker, cluster);
                mClusterToMarker.put(cluster, marker);
                markerWithPosition = new MarkerWithPosition(marker);
                if (animateFrom != null) {
                    markerModifier.animate(markerWithPosition, animateFrom, cluster.getPosition());
                }
            } else {
                markerWithPosition = new MarkerWithPosition(marker);
            }
            onClusterRendered(cluster, marker);
            newMarkers.add(markerWithPosition);
        }
    }

    /**
     * A Marker and its position. Marker.getPosition() must be called from the UI thread, so this
     * object allows lookup from other threads.
     */
    private static class MarkerWithPosition {
        private final Marker marker;
        private LatLng position;

        private MarkerWithPosition(Marker marker) {
            this.marker = marker;
            position = marker.getPosition();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof MarkerWithPosition) {
                return marker.equals(((MarkerWithPosition) other).marker);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return marker.hashCode();
        }
    }

    private static final TimeInterpolator ANIMATION_INTERP = new DecelerateInterpolator();

    /**
     * Animates a markerWithPosition from one position to another. TODO: improve performance for
     * slow devices (e.g. Nexus S).
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private class AnimationTask extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
        private final MarkerWithPosition markerWithPosition;
        private final Marker marker;
        private final LatLng from;
        private final LatLng to;
        private boolean mRemoveOnComplete;
        private MarkerManager mMarkerManager;

        private double lngDelta;

        private AnimationTask(MarkerWithPosition markerWithPosition, LatLng from, LatLng to) {
            this.markerWithPosition = markerWithPosition;
            this.marker = markerWithPosition.marker;
            this.from = from;
            this.to = to;

            lngDelta = to.longitude - from.longitude;

            // Take the shortest path across the 180th meridian.
            if (Math.abs(lngDelta) > 180) {
                lngDelta -= Math.signum(lngDelta) * 360;
            }
        }

        public void perform() {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimator.setInterpolator(ANIMATION_INTERP);
            valueAnimator.addUpdateListener(this);
            valueAnimator.addListener(this);
            valueAnimator.start();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mRemoveOnComplete) {
                Cluster<T> cluster = mMarkerToCluster.get(marker);
                mClusterToMarker.remove(cluster);
                mMarkerCache.remove(marker);
                mMarkerToCluster.remove(marker);
                mMarkerManager.remove(marker);
            }
            markerWithPosition.position = to;
        }

        public void removeOnAnimationComplete(MarkerManager markerManager) {
            mMarkerManager = markerManager;
            mRemoveOnComplete = true;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float fraction = valueAnimator.getAnimatedFraction();
            double lat = (to.latitude - from.latitude) * fraction + from.latitude;
            double lng = lngDelta * fraction + from.longitude;
            LatLng position = new LatLng(lat, lng);
            marker.setPosition(position);
        }
    }
}
