package ir.ac.kntu.patogh.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ir.ac.kntu.patogh.Activities.BadgesActivity;
import ir.ac.kntu.patogh.Activities.EventActivity;
import ir.ac.kntu.patogh.Activities.HistoryActivity;
import ir.ac.kntu.patogh.Activities.MainActivity;
import ir.ac.kntu.patogh.Activities.SettingsActivity;
import ir.ac.kntu.patogh.Adapters.BadgeAdapter;
import ir.ac.kntu.patogh.Adapters.FavoriteAdapter;
import ir.ac.kntu.patogh.Interfaces.PatoghApi;
import ir.ac.kntu.patogh.R;
import ir.ac.kntu.patogh.Utils.Badge;
import ir.ac.kntu.patogh.Utils.Dorehami;
import ir.ac.kntu.patogh.Utils.EqualSpacingItemDecoration;
import ir.ac.kntu.patogh.Utils.FavoriteEvent;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ProfileFragment extends Fragment implements FavoriteAdapter.FavoriteAdapterOnClickHandler, BadgeAdapter.BadgeAdapterOnClickHandler {


    @BindView(R.id.img_profile_pic)
    ImageView profilePicture;
    @BindView(R.id.tv_profile_page_favorites_gone)
    TextView tvFavoritesGone;
    @BindView(R.id.rv_profile_page_badges)
    RecyclerView rvBadge;
    @BindView(R.id.rv_favorite_events)
    RecyclerView rvFavoriteEvents;
    @BindView(R.id.tv_profile_page_name)
    TextView nameTextView;
    @BindView(R.id.tv_profile_page_adjective)
    TextView adjectiveTextView;
    @BindView(R.id.swipeRefresh_profile)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private FavoriteAdapter favoriteAdapter;
    private BadgeAdapter badgeAdapter;
    private Unbinder unbinder;
    private ProfileViewModel profileViewModel;
    private SharedPreferences sharedPreferences;
    private ArrayList<FavoriteEvent> favoriteEvents;
    private String badge[];
    private String serverResponse;
    String imageId;
    private String[] userLvl;
    private String baseURL = "http://patogh.potatogamers.ir:7701/api/";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            getActivity().setTheme(R.style.DarkTheme);

        } else {
            getActivity().setTheme(R.style.LightTheme);
        }
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        favoriteEvents = new ArrayList<>();
        userLvl = new String[]{"تازه وارد", "تجربه اولی", "با تجربه", "خفن", "حرفه ای", "همه فن حریف"};
        unbinder = ButterKnife.bind(this, root);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFavorites();
                getUserDetails();
                setProfilePicture();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.patoghYellow, R.color.patoghBlue);
        sharedPreferences = getActivity()
                .getSharedPreferences("TokenPref", 0);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        rvFavoriteEvents.setLayoutManager(layoutManager);
        favoriteAdapter = new FavoriteAdapter(this);
        rvFavoriteEvents.setAdapter(favoriteAdapter);
        ViewCompat.setNestedScrollingEnabled(rvFavoriteEvents, false);
        rvFavoriteEvents.addItemDecoration(new EqualSpacingItemDecoration(40));
        sharedPreferences = getActivity()
                .getSharedPreferences("TokenPref", 0);


        Toolbar toolbar = root.findViewById(R.id.toolbar_profile_page);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        setHasOptionsMenu(true);


        LinearLayoutManager badgeLayoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvBadge.setLayoutManager(badgeLayoutManager);
        badgeAdapter = new BadgeAdapter(this);
        rvBadge.setAdapter(badgeAdapter);
        rvBadge.addItemDecoration(new EqualSpacingItemDecoration(24));
        loadBadges();

        getUserDetails();
        getFavorites();
        setProfilePicture();

        return root;
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_gear) {
            Context context = getContext();
            Intent intent = new Intent(context, SettingsActivity.class);
            startActivity(intent);
            return true;
        }


        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
        getFavorites();
    }


    private void loadBadges() {
        ArrayList<Badge> badges = new ArrayList<>();
        badges.add(new Badge(R.drawable.ic_sport_badges, 1));
        badges.add(new Badge(R.drawable.ic_win, 2));
        badges.add(new Badge(R.drawable.ic_achievement, 3));
        badges.add(new Badge(R.drawable.ic_success, 4));
        badgeAdapter.setEventData(badges);
    }

    private void showEventDataView() {
        rvFavoriteEvents.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(FavoriteEvent selectedEvent) {
        Context context = getContext();
        Intent intent = new Intent(context, EventActivity.class);
        intent.putExtra("event_name", selectedEvent.getName());
        intent.putExtra("event_date", selectedEvent.getDate());
        intent.putExtra("event_capacity", selectedEvent.getCapacity());
        intent.putExtra("event_id", selectedEvent.getId());
        intent.putExtra("class", "favorite");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            context.startActivity(intent);
        }
    }


    @Override
    public void onClick(Badge selectedBadge) {
        Intent intent = new Intent(getActivity(), BadgesActivity.class);
        startActivity(intent);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    private void getFavorites() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .build();
        Gson gson = new Gson();
        PatoghApi patoghApi = retrofit.create(PatoghApi.class);
        String token = sharedPreferences.getString("Token", "none");
        if (token.equals("none")) {
            Toast.makeText(getContext(), "توکن شما پایان یافته.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }

        showEventDataView();
        patoghApi.getFavorites("Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String res = response.body().string();
                    if (serverResponse != null && serverResponse.equals(res)) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        return;
                    } else {
                        favoriteAdapter.clear();
                        favoriteEvents.clear();
                        serverResponse = res;
                    }

                    JsonObject jsonObject1 = new Gson().fromJson(res, JsonObject.class);
                    String returnValue = jsonObject1.get("returnValue").toString();
                    Type dorehamiType = new TypeToken<ArrayList<Dorehami>>() {
                    }.getType();
                    ArrayList<Dorehami> dorehamis = gson.fromJson(returnValue, dorehamiType);
                    for (Dorehami dorehami : dorehamis) {
                        favoriteEvents.add(new FavoriteEvent(dorehami.getName(), dorehami.getStartTime()
                                , String.format("ظرفیت باقی مانده : %d نفر", dorehami.getSize())
                                , dorehami.getId(), dorehami.getThumbnailId()));
                    }
                    System.out.println(favoriteEvents.size());
                    if (favoriteEvents.size() == 0) {
                        tvFavoritesGone.setVisibility(View.VISIBLE);
                    } else {
                        tvFavoritesGone.setVisibility(View.GONE);
                    }
                    favoriteAdapter.addAll(favoriteEvents);
                    mSwipeRefreshLayout.setRefreshing(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void getUserDetails() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .build();
        PatoghApi patoghApi = retrofit.create(PatoghApi.class);
        String token = sharedPreferences.getString("Token", "none");
        if (token.equals("none")) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
        }

        patoghApi.getUserDetails("Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    System.out.println(response.body());
                    String res = response.body().string();
                    System.out.println(res);
                    JsonObject jsonObject1 = new Gson().fromJson(res, JsonObject.class);
                    String returnValue = jsonObject1.get("returnValue").toString();
                    JsonObject jsonObject2 = new Gson().fromJson(returnValue, JsonObject.class);
                    String firstName = jsonObject2.get("firstName").getAsString();
                    int userLevel = jsonObject2.get("userLevel").getAsInt();
                        imageId = jsonObject2.get("profilePictureId").toString();

                    if (firstName != null) {
                        nameTextView.setText(firstName);
                    }
                    adjectiveTextView.setText(userLvl[userLevel]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    public void setProfilePicture() {

        File file = new File(Environment.getExternalStorageDirectory(), "PATOGH/Pictures/profile.jpg");
        if (!file.exists()) {
            Glide.with(this)
                    .load(getResources().getDrawable(R.drawable.ic_profile_pic))
                    .transform(new RoundedCornersTransformation(22, 0))
                    .into(profilePicture);
        } else {
            Glide.with(this)
                    .load(file)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transform(new RoundedCornersTransformation(22, 0))
                    .into(profilePicture);
        }
    }
}

