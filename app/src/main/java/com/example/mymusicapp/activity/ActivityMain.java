package com.example.mymusicapp.activity;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.example.mymusicapp.MusicProvider;
import com.example.mymusicapp.MusicService;
import com.example.mymusicapp.PlaybackController;
import com.example.mymusicapp.R;
import com.example.mymusicapp.adapter.AdapterArtist;
import com.example.mymusicapp.adapter.AdapterMyPager;
import com.example.mymusicapp.adapter.AdapterPlayList;
import com.example.mymusicapp.adapter.AdapterSong;
import com.example.mymusicapp.entity.Artist;
import com.example.mymusicapp.entity.Playlist;
import com.example.mymusicapp.entity.Song;
import com.example.mymusicapp.fragment.FragmentArtistSongs;
import com.example.mymusicapp.fragment.FragmentArtists;
import com.example.mymusicapp.fragment.FragmentMediaControl;
import com.example.mymusicapp.fragment.FragmentPlaylist;
import com.example.mymusicapp.fragment.FragmentPlaylistSongs;
import com.example.mymusicapp.fragment.FragmentSelectSongs;
import com.example.mymusicapp.fragment.FragmentSongs;
import com.example.mymusicapp.fragment.FragmentTimerPicker;
import com.example.mymusicapp.model.ModelSelectedItem;
import com.example.mymusicapp.repository.DBMusicHelper;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ActivityMain extends AppCompatActivity implements MusicService.ServiceCallbacks,
        AdapterSong.SongItemClickListener, AdapterArtist.ArtistItemClickListener,
        AdapterPlayList.PlaylistClickListener, TimePickerDialog.OnTimeSetListener {

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    AppBarLayout appBarLayout;
    AdapterMyPager pagerAdapter;
    public static MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    public static ArrayList<Song> all_songs;
    public static ArrayList<Song> songs;
    public static ArrayList<Artist> artists;
    public static ArrayList<Playlist> playLists;

    public static ArrayList<ModelSelectedItem> modelSelectedItems;
    LinearLayout layout_mini_controller;
    MusicProvider musicProvider;
    DBMusicHelper dbMusicHelper;
    PlaybackController playbackController;

    FragmentSongs fragmentSongs;
    FragmentArtists fragmentArtists;
    FragmentPlaylist fragmentPlaylist;
    FragmentPlaylistSongs fragmentPlaylistSongs;
    FragmentArtistSongs fragmentArtistSongs;
    FragmentSelectSongs fragmentSelectSongs;
    FragmentMediaControl fragmentMediaControl;
    FragmentTimerPicker fragmentTimerPicker;
    private int[] tabIcons = {R.drawable.ic_audiotrack, R.drawable.ic_star, R.drawable.ic_featured_play_list};
    private int[] tabTitles = {R.string.songs, R.string.artists, R.string.playlists};
    String name, check;
    public Locale myLocale;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor shaEditor;
    String prefer_lang;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appBarLayout = findViewById(R.id.layout_appbar);
        tabLayout = findViewById(R.id.tab_layout);
        toolbar = findViewById(R.id.toolbar_main);
        viewPager = findViewById(R.id.pager);

        setSupportActionBar(toolbar);
        pagerAdapter = new AdapterMyPager(getSupportFragmentManager());
        pagerAdapter.addFragment(new FragmentSongs(), getString(R.string.songs));
        pagerAdapter.addFragment(new FragmentArtists(), getString(R.string.artists));
        pagerAdapter.addFragment(new FragmentPlaylist(), getString(R.string.playlists));
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        setIconForTabTitle();
        layout_mini_controller = findViewById(R.id.layout_mini_controller);
        layout_mini_controller.setVisibility(View.GONE);

        musicProvider = new MusicProvider(this);
        all_songs = musicProvider.loadSongs();
        songs = all_songs;
        artists = musicProvider.loadArtist();

        dbMusicHelper = new DBMusicHelper(ActivityMain.this);
        playLists = dbMusicHelper.getAllPlaylists();
        playbackController = new PlaybackController(layout_mini_controller);

        fragmentSongs = (FragmentSongs) pagerAdapter.getItem(0);
        fragmentArtists = (FragmentArtists) pagerAdapter.getItem(1);
        fragmentPlaylist = (FragmentPlaylist) pagerAdapter.getItem(2);
        initModelSelectedItems(songs.size());
//        name = getIntent().getStringExtra("name");
//        check = getIntent().getStringExtra("check");

        sharedPreferences = getSharedPreferences(ActivityLogin.MY_PREFS_FILENAME, MODE_PRIVATE);
        name = sharedPreferences.getString(ActivityLogin.NAME, "");
        check = sharedPreferences.getString(ActivityLogin.CHECK, "");
        prefer_lang = sharedPreferences.getString("prefer_lang", "en");
        setLocale(prefer_lang);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0) {
                    songs = musicProvider.loadSongs();
                    fragmentSongs.getAdapterSong().setList(songs);
                    fragmentSongs.getAdapterSong().setModel(initModelSelectedItems(songs.size()));
                    musicSrv.setList(songs);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setIconForTabTitle() {
        for(int i = 0 ; i< tabIcons.length; i++) {
            tabLayout.getTabAt(i).setIcon(tabIcons[i]);
//            tabLayout.getTabAt(i).setText(tabTitles[i]);
        }
    }

    public ArrayList<ModelSelectedItem> initModelSelectedItems(int size) {
        modelSelectedItems = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            modelSelectedItems.add(new ModelSelectedItem(i, false));
        }
        return modelSelectedItems;
    }

    public void highlightCurrentPosition() {
        fragmentSongs.changeSongItemDisplay(musicSrv.getPlayingSongPos());
    }

    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setList(songs);
            musicBound = true;
            musicSrv.setCallBacks(ActivityMain.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    private Menu menu;
    MenuItem it_refresh, it_new_playlist, it_sleep_timer, it_add_to_other_playlist,
            it_add_to_this_playlist, it_delete_from_playlist , it_deselect_item, it_my_account,
            it_language, it_vn, it_eng, it_search;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        findMenuItem();
        setOnClickListenerForMenuItem();
        SearchView searchView = (SearchView) it_search.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setQueryHint(getString(R.string.search_song_name));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fragmentSongs.getAdapterSong().getFilter().filter(newText);
                if (fragmentPlaylistSongs != null) {
                    fragmentPlaylistSongs.getAdapterSong().getFilter().filter(newText);
                }
                if (fragmentArtistSongs != null) {
                    fragmentArtistSongs.getAdapterSong().getFilter().filter(newText);
                }
                return false;
            }
        });
        return true;
    }

    public void findMenuItem() {
        it_search = menu.findItem(R.id.menu_search);
        it_my_account = menu.findItem(R.id.menu_my_account);
        it_refresh = menu.findItem(R.id.it_refresh);
        it_new_playlist = menu.findItem(R.id.it_new_playlist);
        it_sleep_timer = menu.findItem(R.id.it_sleep_timer);
        it_add_to_other_playlist = menu.findItem(R.id.it_add_to_other_playlist);
        it_add_to_this_playlist = menu.findItem(R.id.it_add_song_to_this_playlist);
        it_delete_from_playlist = menu.findItem(R.id.it_delete_from_playlist);
        it_deselect_item = menu.findItem(R.id.it_deselect_item);

        it_language = menu.findItem(R.id.it_change_language);
        it_eng = menu.findItem(R.id.it_eng);
        it_vn = menu.findItem(R.id.it_vn);

        if(name == null || name.length() == 0) it_my_account.setTitle(R.string.login);
        else it_my_account.setTitle(name);
    }

    private void setOnClickListenerForMenuItem() {
        it_my_account.setOnMenuItemClickListener( menuItem -> {
            Intent accountIntent = new Intent(ActivityMain.this, ActivityAccount.class);
            accountIntent.putExtra(ActivityLogin.NAME, name);
            accountIntent.putExtra(ActivityLogin.CHECK, check);
            startActivity(accountIntent);
            finish();
            return true;
        });
        it_new_playlist.setOnMenuItemClickListener( menuItem -> {
            onClickAddNewPlaylist();
            return true;
        });
        it_add_to_this_playlist.setOnMenuItemClickListener(menuItem -> {
            onClickOptionAddSongs(fragmentPlaylistSongs.getPlaylist_pos());
            return true;
        });
        it_add_to_other_playlist.setOnMenuItemClickListener(menuItem -> {
            onClickOptionAddToPlaylist();
            return true;
        });
        it_delete_from_playlist.setOnMenuItemClickListener(menuItem -> {
            deleteFromPlaylist();
            return true;
        });
        it_deselect_item.setOnMenuItemClickListener(menuItem -> {
            cancelSelected();
            return true;
        });

        it_eng.setOnMenuItemClickListener(menuItem -> {
            setLocale("en");
            shaEditor = sharedPreferences.edit();
            shaEditor.putString("prefer_lang", "en");
            shaEditor.apply();
            return true;
        });
        it_vn.setOnMenuItemClickListener(menuItem -> {
            setLocale("vi");
            shaEditor = sharedPreferences.edit();
            shaEditor.putString("prefer_lang", "vi");
            shaEditor.apply();
            return true;
        });

        it_refresh.setOnMenuItemClickListener(menuItem -> {
            refresh();
            return true;
        });

        it_sleep_timer.setOnMenuItemClickListener(menuItem -> {
            fragmentTimerPicker = new FragmentTimerPicker();
            fragmentTimerPicker.show(getSupportFragmentManager(), "Timer picker");
            return true;
        });
    }

    public void refresh() {
        all_songs = musicProvider.loadSongs();
        songs = all_songs;
        artists = musicProvider.loadArtist();
        playLists = dbMusicHelper.getAllPlaylists();
        fragmentSongs.getAdapterSong().setList(songs);
        if (fragmentArtists != null) {
            fragmentArtists.getAdapterArtist().setList(artists);
        }
        if (fragmentPlaylist != null) {
            fragmentPlaylist.getAdapterPlayList().setList(playLists);
        }
    }

    public void changeMenuWhenSelectMultipleItem() {
        it_refresh.setVisible(false);
        it_new_playlist.setVisible(false);
        it_sleep_timer.setVisible(false);
        it_add_to_other_playlist.setVisible(true);
        if(viewPager.getCurrentItem() == 2) {
            it_delete_from_playlist.setVisible(true);
        }else {
            it_delete_from_playlist.setVisible(false);
        }
        it_deselect_item.setVisible(true);
    }

    public void changeMenuInPlaylistDetails() {
        it_refresh.setVisible(true);
        it_new_playlist.setVisible(true);
        it_sleep_timer.setVisible(true);
        if(viewPager.getCurrentItem() == 2) {
            it_add_to_this_playlist.setVisible(true);
        } else {
            it_add_to_this_playlist.setVisible(false);
        }
        it_delete_from_playlist.setVisible(false);
        it_deselect_item.setVisible(false);
    }

    public void recoverMenu() {
        it_refresh.setVisible(true);
        it_new_playlist.setVisible(true);
        it_sleep_timer.setVisible(true);
        it_add_to_this_playlist.setVisible(false);
        it_add_to_other_playlist.setVisible(false);
        it_delete_from_playlist.setVisible(false);
        it_deselect_item.setVisible(false);
    }

    @Override
    public void onPlayNewSong() {
        highlightCurrentPosition();
    }

    @Override
    public void onMusicPause() {
        highlightCurrentPosition();
    }

    @Override
    public void onMusicResume() {
        highlightCurrentPosition();
    }

    @Override
    public void onSongItemClick(int position) {
        playbackController.songPicked(position);
    }

    @Override
    public void onMultipleSelected() {
        changeMenuWhenSelectMultipleItem();
    }

    public void maximizeMediaController(View view) {
        fragmentMediaControl = new FragmentMediaControl();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.layout_main, fragmentMediaControl);
        transaction.addToBackStack(null);
        transaction.commit();
        appBarLayout.setVisibility(View.GONE);
        layout_mini_controller.setVisibility(View.GONE);
    }

    public void popStackedFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(musicConnection);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            appBarLayout.setVisibility(View.VISIBLE);
            layout_mini_controller.setVisibility(View.VISIBLE);
        } else {
            recoverFragment();
            recoverMenu();
            setIconForTabTitle();
        }
    }

    public void recoverFragment() {
        if(viewPager.getCurrentItem() == 1) {
            pagerAdapter.get_list_fragment().remove(1);
            pagerAdapter.get_list_fragment().add(1, fragmentArtists);
            pagerAdapter.notifyDataSetChanged();
        } else if(viewPager.getCurrentItem() == 2) {
            pagerAdapter.get_list_fragment().remove(2);
            pagerAdapter.get_list_fragment().add(2, fragmentPlaylist);
            pagerAdapter.notifyDataSetChanged();
        }
    }

    //add song to any playlist
    public void onClickOptionAddToPlaylist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_selected_song_to_playlist);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);
        EditText editText = new EditText(layout.getContext());
        editText.setHint(R.string.enter_new_playlist);
        editText.setPadding(20,20,20,20);
        layout.addView(editText);
        Set<Integer> set_selected_playlist = new HashSet<>();
        for (Playlist playlist : playLists) {
            TextView textView = new TextView(layout.getContext());
            textView.setTextSize(20);
            textView.setPadding(10, 10, 10, 10);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setText(playlist.getName());
            layout.addView(textView);
            textView.setOnClickListener(v -> {
                int selectedId = playLists.indexOf(playlist);
                if (set_selected_playlist.contains(selectedId)) {
                    v.setBackgroundColor(Color.WHITE);
                    set_selected_playlist.remove(selectedId);
                } else {
                    v.setBackgroundColor(Color.CYAN);
                    set_selected_playlist.add(selectedId);
                }
            });
        }
        builder.setView(layout);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String playlist_name = editText.getText().toString();
            if (!playlist_name.isEmpty()) {
                //add playlist
                dbMusicHelper.addPlaylist(new Playlist(playlist_name));
                playLists = dbMusicHelper.getAllPlaylists();
                //add song to new playlist
                addSelectedSongToPlaylist(playLists.size() - 1);
            } else {
                for (Integer index : set_selected_playlist) {
                    addSelectedSongToPlaylist(index);
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    //playlist operation
    String playlist_name = "";
    public void onClickAddNewPlaylist() {
        //show input dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enter_new_playlist);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                playlist_name = input.getText().toString().trim();
                if(playlist_name.length() == 0) {
                    Toast.makeText(ActivityMain.this, R.string.enter_playlist_name, Toast.LENGTH_SHORT).show();
                } else {
                    //add playlist
                    Playlist playlist = new Playlist();
                    playlist.setName(playlist_name);
                    if (playLists.stream().noneMatch(pls -> pls.getName().equals(playlist_name))) {
                        playLists.add(playlist);
                        dbMusicHelper.addPlaylist(playlist);
                        if(fragmentPlaylist != null) {
                            fragmentPlaylist.getAdapterPlayList().notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(ActivityMain.this, getString(R.string.playlist_name_duplicate), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
        layout_mini_controller.setVisibility(View.GONE);
    }

    public void updatePlaylistName(int position) {
        Playlist playlist = playLists.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.enter_new_playlist);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setPadding(50,50,50,50);
        input.setText(playlist.getName());
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                playlist_name = input.getText().toString();
                if (playlist_name.length() == 0) {
                    Toast.makeText(ActivityMain.this, R.string.enter_playlist_name, Toast.LENGTH_SHORT).show();
                } else {
                    //rename playlist
                    playlist.setName(playlist_name);
                    dbMusicHelper.updatePlaylist(playlist);
                    fragmentPlaylist.getAdapterPlayList().notifyDataSetChanged();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
        layout_mini_controller.setVisibility(View.GONE);
    }

    @Override
    public void onClickPlaylistItem(int position) {
        changeMenuInPlaylistDetails();
        fragmentPlaylistSongs = new FragmentPlaylistSongs();
        Bundle bundle = new Bundle();
        bundle.putInt("playlist_pos", position);
        fragmentPlaylistSongs.setArguments(bundle);
        pagerAdapter.get_list_fragment().remove(2);
        pagerAdapter.get_list_fragment().add(2, fragmentPlaylistSongs);
        pagerAdapter.notifyDataSetChanged();
        setIconForTabTitle();
    }

    public void deletePlaylist(int position) {
        int playlistId = playLists.get(position).getId();
        playLists.remove(position);
        dbMusicHelper.deletePlaylist(playlistId);
    }

    //artist operation
    @Override
    public void onClickArtistItem(int position) {
        String artistName = artists.get(position).getName();
        fragmentArtistSongs = new FragmentArtistSongs();
        Bundle bundle = new Bundle();
        bundle.putString("artist", artistName);
        fragmentArtistSongs.setArguments(bundle);
        pagerAdapter.get_list_fragment().remove(1);
        pagerAdapter.get_list_fragment().add(1, fragmentArtistSongs);
        pagerAdapter.notifyDataSetChanged();
        setIconForTabTitle();
    }

    public void onClickOptionAddSongs(int position) {
        layout_mini_controller.setVisibility(View.GONE);
        fragmentSelectSongs = new FragmentSelectSongs();
        Bundle bundle = new Bundle();
        bundle.putInt("playlist_pos", position);
        fragmentSelectSongs.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.layout_main, fragmentSelectSongs);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    public void addSelectedSongToPlaylist(int position){
        int playlistId = playLists.get(position).getId();
        ArrayList<Song> selectedSongs = new ArrayList<>();
        for (ModelSelectedItem item : modelSelectedItems) {
            if (item.isSelectd()) {
                selectedSongs.add(songs.get(item.getPosition()));
            }
        }
        dbMusicHelper.addSongsToPlaylist(selectedSongs, playlistId);
        if (fragmentPlaylistSongs != null && position == fragmentPlaylistSongs.getPlaylist_pos()) {
            ArrayList<Song> playlistSongs = dbMusicHelper.getPlaylistSongs(playlistId);
            fragmentPlaylistSongs.getAdapterSong().setList(playlistSongs);
            fragmentPlaylistSongs.getAdapterSong().setModel(initModelSelectedItems(playlistSongs.size()));
        }
        popStackedFragment();
    }

    public void deleteFromPlaylist() {
        ArrayList<Song> selectedSongs = new ArrayList<>();
        Song song;
        for(ModelSelectedItem modelSelectedItem: modelSelectedItems) {
            if(modelSelectedItem.isSelectd()) {
                song = musicSrv.getSongs().get(modelSelectedItem.getPosition());
                selectedSongs.add(song);
            }
        }
        int playlistId = playLists.get(fragmentPlaylistSongs.getPlaylist_pos()).getId();
        dbMusicHelper.deleteSongsFromPlaylist(playlistId, selectedSongs);
        songs = dbMusicHelper.getPlaylistSongs(playlistId);
        fragmentPlaylistSongs.getAdapterSong().setList(songs);
        musicSrv.setList(songs);
    }

    public void cancelSelected() {
        if (viewPager.getCurrentItem() == 0) {
            fragmentSongs.getAdapterSong().setMultiSelected(false);
            fragmentSongs.getAdapterSong().setModel(initModelSelectedItems(songs.size()));
        } else if (viewPager.getCurrentItem() == 1) {
            fragmentArtistSongs.getAdapterSong().setMultiSelected(false);
            fragmentArtistSongs.getAdapterSong().setModel(initModelSelectedItems(songs.size()));
        } else {
            fragmentPlaylistSongs.getAdapterSong().setMultiSelected(false);
            fragmentPlaylistSongs.getAdapterSong().setModel(initModelSelectedItems(songs.size()));
        }
        recoverMenu();
    }

    public DBMusicHelper getDbMusicHelper() {
        return dbMusicHelper;
    }

    public MusicProvider getMusicProvider() {
        return musicProvider;
    }

    public int getCurrentPagePosition() {
        return viewPager.getCurrentItem();
    }

    public void setLocale(String lang) {
        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration config = res.getConfiguration();
        if (!config.locale.getLanguage().equals(myLocale.getLanguage())) {
            config.locale = myLocale;
            res.updateConfiguration(config, dm);
            Intent refresh = new Intent(this, ActivityMain.class);
            refresh.putExtras(bundle());
            startActivity(refresh);
        }
    }

    public Bundle bundle() {
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("check", check);
        return bundle;
    }

    CountDownTimer countDownTimer;
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        int milliseconds = (hourOfDay * 3600 + minute * 60) * 1000;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(milliseconds, 1000) {
            long r_hour, r_min, r_sec, secs;
            String s_hour, s_min, s_sec;

            public void onTick(long millisUntilFinished) {
                secs = millisUntilFinished / 1000;
                r_hour = secs / 3600;
                r_min = (secs % 3600) / 60;
                r_sec = secs % 60;
                s_hour = String.valueOf(r_hour);
                s_min = String.valueOf(r_min);
                s_sec = String.valueOf(r_sec);
                if (r_hour < 10) s_hour = "0" + r_hour;
                if (r_min < 10) s_min = "0" + r_min;
                if (r_sec < 10) s_sec = "0" + r_sec;
                it_sleep_timer.setTitle(String.format("%s: %s:%s:%s", getString(R.string.timer), s_hour, s_min, s_sec));
            }

            public void onFinish() {
                it_sleep_timer.setTitle(R.string.set_timer);
                playbackController.stop();
            }
        };
        countDownTimer.start();
    }
}