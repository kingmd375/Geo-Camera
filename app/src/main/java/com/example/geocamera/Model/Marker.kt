package com.example.geocamera.Model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marker_table")
class Marker (
    //Note that we now allow for ID as the primary key
    //It needs to be nullable when creating a new word in the database
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "marker_location") var markerName: String,
    @ColumnInfo(name = "marker_image_path") var markerImagePath: String,
    @ColumnInfo(name = "marker_date") var markerDate: String,
    @ColumnInfo(name = "marker_description") var markerDescription: String
)