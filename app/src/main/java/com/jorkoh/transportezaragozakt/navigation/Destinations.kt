//package com.jorkoh.transportezaragozakt.navigation
//
//import androidx.fragment.app.Fragment
//import com.jorkoh.transportezaragozakt.R
//import com.jorkoh.transportezaragozakt.destinations.favorites.FavoritesFragment
//import com.jorkoh.transportezaragozakt.destinations.map.MapFragment
//import com.jorkoh.transportezaragozakt.destinations.more.MoreFragment
//import com.jorkoh.transportezaragozakt.destinations.search.SearchFragment
//
//enum class Destinations {
//    Favorites {
//        override fun getFragment(): Fragment {
//            return FavoritesFragment.newInstance()
//        }
//
//        override fun getTag(): String {
//            return FavoritesFragment.DESTINATION_TAG
//        }
//
//        override fun getMenuItemID(): Int {
//            return R.id.navigation_favorites
//        }
//
//        override fun toString(): String {
//            return getTag()
//        }
//    },
//    Map {
//        override fun getFragment(): Fragment {
//            return MapFragment.newInstance()
//        }
//
//        override fun getTag(): String {
//            return MapFragment.DESTINATION_TAG
//        }
//
//        override fun getMenuItemID(): Int {
//            return R.id.navigation_map
//        }
//
//        override fun toString(): String {
//            return getTag()
//        }
//    },
//    Search {
//        override fun getFragment(): Fragment {
//            return SearchFragment.newInstance()
//        }
//
//        override fun getTag(): String {
//            return SearchFragment.DESTINATION_TAG
//        }
//
//        override fun getMenuItemID(): Int {
//            return R.id.navigation_search
//        }
//
//        override fun toString(): String {
//            return getTag()
//        }
//    },
//    More {
//        override fun getFragment(): Fragment {
//            return MoreFragment.newInstance()
//        }
//
//        override fun getTag(): String {
//            return MoreFragment.DESTINATION_TAG
//        }
//
//        override fun getMenuItemID(): Int {
//            return R.id.navigation_more
//        }
//
//        override fun toString(): String {
//            return getTag()
//        }
//    };
//
//    abstract fun getFragment(): Fragment
//    abstract fun getTag(): String
//    abstract fun getMenuItemID(): Int
//
//    companion object {
//        fun getMainDestination(): Destinations =
//            Favorites
//    }
//}