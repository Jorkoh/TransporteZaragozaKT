package com.jorkoh.transportezaragozakt.services.common.responses

import com.jorkoh.transportezaragozakt.db.RuralTracking

interface RuralTrackingsResponse {
    fun toRuralTrackings(): List<RuralTracking>
}