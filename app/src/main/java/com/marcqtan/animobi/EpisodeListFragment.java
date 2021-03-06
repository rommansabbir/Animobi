package com.marcqtan.animobi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Marc Q. Tan on 27/03/2018.
 */

public class EpisodeListFragment extends Fragment implements EpisodeListAdapter.onItemClicked, Utility.interface2 {

    RecyclerView episode_list;
    EpisodeListAdapter epadapter;
    FrameLayout frame;
    //TextView summary;
    Anime anime;
    ListView list;
    ImageView myimage;
    ProgressBar progress;
    AppBarLayout appbar;
    AsyncTask task = null;
    CollapsingToolbarLayout collapsetoolbar;

    public EpisodeListFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_episode_list, container, false);
        episode_list = rootView.findViewById(R.id.episodeRV);
        frame = rootView.findViewById(R.id.progressBarContainer);
        //summary = rootView.findViewById(R.id.summary);
        myimage = rootView.findViewById(R.id.myimage);
        progress = rootView.findViewById(R.id.progressBar);
        appbar = rootView.findViewById(R.id.appbar);
        collapsetoolbar = rootView.findViewById(R.id.collapsing_toolbar);

        anime = (Anime) getArguments().getSerializable("anime");
        Utility.initCollapsingToolbar(collapsetoolbar, appbar, anime.getAnimeName());

        //summary.setText(anime.getSummary());


        if(!getArguments().getString("transitionName").equals("")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                myimage.setTransitionName(getArguments().getString("transitionName"));
            }

            /*Glide.with(this)
                    .load(anime.getAnimeThumbnail())
                    .dontAnimate()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            scheduleStartPostponedTransition(myimage);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            scheduleStartPostponedTransition(myimage);
                            return false;
                        }
                    })
                    .into(myimage);*/

            Picasso.with(getActivity())
                    .load(anime.getAnimeThumbnail())
                    .noFade()
                    //.memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                    //.networkPolicy(NetworkPolicy.NO_CACHE)
                    .into(myimage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    getActivity().supportStartPostponedEnterTransition();
                                }

                                @Override
                                public void onError() {
                                    getActivity().supportStartPostponedEnterTransition();
                                }
                    });

        }
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
       // Utility.initCollapsingToolbar((CollapsingToolbarLayout)rootView.findViewById(R.id.collapsing_toolbar),(AppBarLayout)rootView.findViewById(R.id.appbar), getString(R.string.app_name));

        LinearLayoutManager lm = new LinearLayoutManager(getActivity());

        episode_list.setLayoutManager(lm);
        episode_list.setHasFixedSize(true);

        epadapter = new EpisodeListAdapter(anime.retrieveEpisodes(), this);

        episode_list.setAdapter(epadapter);
        return rootView;
    }

    private void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(anime.retrieveEpisodes() == null) {
            task = new Utility.getAnimeEpisode(this).execute(anime);
        } else {
            epadapter.setEpisodeListData(anime.retrieveEpisodes());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(task != null) {
            task.cancel(true);
        }
    }

    @Override
    public void onClick(int position) {
        //String episodeUrl = lists_episode.get(position).getValue();
        //String episodeName = lists_episode.get(position).getKey();
        new getAnimeVideo(this).execute(anime.retrieveEpisodes().get(position).getValue());
        //Toast.makeText(this,"LINK IS " + lists_episode.get(position).getValue(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showVisibilty() {
        frame.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideVisibility() {
        frame.setVisibility(View.GONE);
    }

    @Override
    public Context getCtx() {
        return getActivity();
    }

    @Override
    public Activity getActvty() {
        return getActivity();
    }


    static class getAnimeVideo extends AsyncTask<String, Void, EpisodeWrapper> {
        List<String> quality_name = new ArrayList<String>();

        private WeakReference<EpisodeListFragment> activity;

        getAnimeVideo(EpisodeListFragment activity) {
            this.activity = new WeakReference<EpisodeListFragment>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activity.get().frame.setVisibility(View.VISIBLE);
        }

        @Override
        protected EpisodeWrapper doInBackground(String... params) {
            try {
                Document doc = Jsoup.connect(params[0]).timeout(30000).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36 OPR/48.0.2685.50")
                        .followRedirects(true)
                        .get();

                List<String> qualityList = Utility.getQuality(doc, quality_name);
                EpisodeWrapper ew = new EpisodeWrapper();
                ew.qualityList = qualityList;
                ew.doc = doc;

                return ew;
            } catch (IOException e) {
                e.printStackTrace();
                Log.v("getAnimeEpisode()", "Error accessing link");
                return null;
            }
        }

        @Override
        protected void onPostExecute(EpisodeWrapper ew) {
            super.onPostExecute(ew);
            activity.get().frame.setVisibility(View.GONE);

            if(ew.qualityList == null || ew.qualityList.size() == 0) { //Try fetching from another source
                new getVidAnotherSource(activity.get()).execute(ew.doc);
            } else if (ew.qualityList.size() == 1) {

                if(activity.get().getActivity() == null) {
                    return;
                }

                Intent i = new Intent(activity.get().getActivity(), exoactivity.class);
                i.putExtra("vidurl", ew.qualityList.get(0));
                i.putExtra("animeName", activity.get().anime.getAnimeName());
                activity.get().startActivity(i);

            } else {
                Utility.showBottomSheet(activity.get(), ew.qualityList, quality_name, activity.get().anime.getAnimeName());
            }
        }
    }

    static class getVidAnotherSource extends AsyncTask<Document, Void, String> {
        private WeakReference<EpisodeListFragment> activity;
        ProgressDialog progressDialog;

        getVidAnotherSource(EpisodeListFragment activity) {
            this.activity = new WeakReference<EpisodeListFragment>(activity);
            progressDialog = new ProgressDialog(activity.getContext());
            progressDialog.setTitle("No available video stream");
            progressDialog.setMessage("Fetching from other sources...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
            //activity.get().frame.setVisibility(View.VISIBLE);
            //fetching dialog progress;
        }

        @Override
        protected String doInBackground(Document... params) {
            try {
                String vidLink = null;
                String javascript = params[0].getElementsByClass("video-wrap").get(0).select("script").html();

                if(!javascript.equals("")) {
                    String lineWithoutSpaces = javascript.replaceAll("\\s+", ""); //remove whitespace

                    Pattern p = Pattern.compile("\"([^\"]*)\""); //get everything inside a quotation.
                    Matcher m = p.matcher(lineWithoutSpaces);

                    String vidLoxtmpLink = null;

                    while (m.find()) {
                        if (m.group(1).equals("Vidlox")) {
                            if (m.find()) {
                                vidLoxtmpLink = m.group(1); //get vidLox link
                                break;
                            }
                        }
                    }

                    if(vidLoxtmpLink != null) {
                        String url = "https://otakustream.tv" + vidLoxtmpLink;

                        Document doc2 = Jsoup.connect(url).timeout(30000).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36 OPR/48.0.2685.50")
                                .followRedirects(true)
                                .get();

                        String vidLoxEmbedLink = doc2.select("iframe").attr("src");

                        Document doc3 = Jsoup.connect(vidLoxEmbedLink).timeout(30000).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36 OPR/48.0.2685.50")
                                .followRedirects(true)
                                .get();

                        String script = doc3.select("script").last().html(); //the last javascript has the vidLox directLink;
                        String scriptnospaces = script.replaceAll("\\s+", ""); //remove whitespace

                        Pattern p2 = Pattern.compile("^https.*\\.mp4$"); //match strings that start with http and ends with mp4

                        Matcher m2 = p.matcher(scriptnospaces);
                        Matcher m3;

                        String vidLoxDirectLink = "";

                        while (m2.find()) {
                            m3 = p2.matcher(m2.group(1));
                            if (m3.find()) {
                                vidLoxDirectLink = m3.group(0); // get the vidLox direct link
                                break;
                            }
                        }
                        if (!vidLoxDirectLink.equals("")) {
                            vidLink = vidLoxDirectLink;
                        }
                    }
                }

                return vidLink;

            } catch (IOException e) {
                e.printStackTrace();
                Log.v("getVidAnotherSource()", "Error accessing link");
                return null;
            }
        }

        @Override
        protected void onPostExecute(String vidUrl) {
            super.onPostExecute(vidUrl);
            progressDialog.dismiss();
            //activity.get().frame.setVisibility(View.GONE);
            //remove progressdialog

            if(vidUrl == null) {
                AlertDialog.Builder adb = new AlertDialog.Builder(activity.get().getActivity());
                adb.setTitle("No available video stream");
                adb.setMessage(":(");
                adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog ad = adb.create();
                ad.show();

                Log.v("Error", "Error fetching video quality url");
            } else {
                if(activity.get().getActivity() == null) {
                    return;
                }

                Intent i = new Intent(activity.get().getActivity(), exoactivity.class);
                i.putExtra("vidurl", vidUrl);
                i.putExtra("animeName", activity.get().anime.getAnimeName());
                activity.get().startActivity(i);

            }
        }
    }
}
