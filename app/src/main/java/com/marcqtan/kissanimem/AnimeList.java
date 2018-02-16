package com.marcqtan.kissanimem;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by dell on 25/10/2017.
 */

class AnimeList {

    private String m_animeName;
    private String m_animeLink;
    private String m_thumbnail;
    private ArrayList<Map.Entry<String,String>> m_episodeList;
    private String m_episode_count;

    String getAnimeName() {
        return m_animeName;
    }

    void setAnimeName(String animename){
        m_animeName = animename;
    }

    void setThumbNail(String image){
        m_thumbnail = image;
    }

    void setEpisodeCount(String m_episode_count){
        this.m_episode_count = m_episode_count;
    }

    String getEpisodeCount(){
        return m_episode_count;
    }

    String getAnimeLink() {
        return m_animeLink;
    }

    String getAnimeThumbnail() {
        return m_thumbnail;
    }

    void setAnimeLink(String animelink){
        m_animeLink = animelink;
    }

    void addEpisodeInfo(Map.Entry<String, String> episode) {
        m_episodeList.add(episode);
    }

    ArrayList<Map.Entry<String,String>> retrieveEpisodes() {
        return m_episodeList;
    }

    void initEpisodeList(){
        m_episodeList = new ArrayList<>();
    }

}
