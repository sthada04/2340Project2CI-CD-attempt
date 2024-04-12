package com.example.spotifywrapped;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WrappedData {
    private List<String> songNames = new ArrayList<>();
    private List<String> artistNames = new ArrayList<>();

    private List<String> genreNames = new ArrayList<>();
    private List<String> albumNames = new ArrayList<>();
    private List<String> orderedGenreNames = new ArrayList<>();
    private Map<String, Integer> genreCountMap = new HashMap<>();

    private List<String> reccommended = new ArrayList<>();
    private String created = "";

    public WrappedData(){
    }

    public WrappedData(List<String> songNames, List<String> albumNames, List<String> artistNames, List<String> genreNames, List<String> orderedGenreNames, Map<String, Integer> genreCountMap, String created, List<String> reccommended) {
        this.songNames = songNames;
        this.albumNames = albumNames;
        this.artistNames = artistNames;
        this.genreNames = genreNames;
        this.orderedGenreNames = orderedGenreNames;
        this.genreCountMap = genreCountMap;
        this.created = created;
        this.reccommended = reccommended;
    }


    public List<String> getSongNames() {
        return songNames;
    }

    public void setSongNames(List<String> songNames) {
        this.songNames = songNames;
    }

    public List<String> getAlbumNames() {
        return albumNames;
    }

    public void setAlbumNames(List<String> albumNames) {
        this.albumNames = albumNames;
    }

    public List<String> getArtistNames() {
        return artistNames;
    }

    public void setArtistNames(List<String> artistNames) {
        this.artistNames = artistNames;
    }

    public List<String> getGenreNames() {
        return genreNames;
    }

    public void setGenreNames(List<String> genreNames) {
        this.genreNames = genreNames;
    }

    public List<String> getOrderedGenreNames() {
        return orderedGenreNames;
    }

    public void setOrderedGenreNames(List<String> orderedGenreNames) {
        this.orderedGenreNames = orderedGenreNames;
    }

    public Map<String, Integer> getGenreCountMap() {
        return genreCountMap;
    }

    public void setGenreCountMap(Map<String, Integer> genreCountMap) {
        this.genreCountMap = genreCountMap;
    }

    public String getCreated() {
        return created;
    }


    public List<String> getReccommended() {
        return reccommended;
    }

    public void setReccommended(List<String> reccommended) {
        this.reccommended = reccommended;
    }
}
