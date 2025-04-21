package com.example.smartchess.play;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartchess.R;
import com.example.smartchess.auth.UserSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsFragment extends Fragment {

    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView friendsRecyclerView;
    private RecyclerView searchResultsRecyclerView;
    private TextView placeholderText;
    private LinearLayout searchResultsContainer;

    private FirebaseFirestore db;
    private String currentUserId;
    private UserSession userSession;

    private FriendsAdapter friendsAdapter;
    private SearchResultsAdapter searchResultsAdapter;
    private List<UserModel> friendsList;
    private List<UserModel> searchResultsList;
    private List<String> currentFriendIds;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userSession = new UserSession(getContext());

        initViews(view);
        setupRecyclerViews();
        setupListeners();

        loadFriendsList();

        return view;
    }

    private void initViews(View view) {
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);
        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view);
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view);
        placeholderText = view.findViewById(R.id.txt_placeholder);
        searchResultsContainer = view.findViewById(R.id.search_results_container);

        searchResultsContainer.setVisibility(View.GONE);
    }

    private void setupRecyclerViews() {
        friendsList = new ArrayList<>();
        searchResultsList = new ArrayList<>();
        currentFriendIds = new ArrayList<>();

        friendsAdapter = new FriendsAdapter(friendsList);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsRecyclerView.setAdapter(friendsAdapter);

        searchResultsAdapter = new SearchResultsAdapter(searchResultsList);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);
    }

    private void setupListeners() {
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = searchEditText.getText().toString().trim();
                if (!searchQuery.isEmpty()) {
                    searchUsers(searchQuery);
                }
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    searchResultsContainer.setVisibility(View.GONE);
                    friendsRecyclerView.setVisibility(View.VISIBLE);
                    placeholderText.setVisibility(friendsList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    private void loadFriendsList() {
        db.collection("users").document(currentUserId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<String> friendIds = (List<String>) document.get("friends");

                                if (friendIds != null && !friendIds.isEmpty()) {
                                    currentFriendIds = new ArrayList<>(friendIds);
                                    loadFriendsDetails(friendIds);
                                } else {
                                    currentFriendIds = new ArrayList<>();
                                    placeholderText.setVisibility(View.VISIBLE);
                                    friendsRecyclerView.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "Erreur lors du chargement des amis", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadFriendsDetails(List<String> friendIds) {
        friendsList.clear();

        for (String friendId : friendIds) {
            db.collection("users").document(friendId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String id = document.getId();
                                    String username = document.getString("username");
                                    long elo = document.getLong("elo") != null ? document.getLong("elo") : 0;
                                    String profilePicture = document.getString("profilePicture");

                                    UserModel friend = new UserModel(id, username, (int) elo, profilePicture);
                                    friendsList.add(friend);

                                    if (friendsList.size() == friendIds.size()) {
                                        friendsAdapter.notifyDataSetChanged();
                                        placeholderText.setVisibility(View.GONE);
                                        friendsRecyclerView.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }
                    });
        }
    }

    private void searchUsers(String query) {
        searchResultsList.clear();

        db.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();

                                if (!id.equals(currentUserId)) {
                                    String username = document.getString("username");
                                    long elo = document.getLong("elo") != null ? document.getLong("elo") : 0;
                                    String profilePicture = document.getString("profilePicture");

                                    boolean isAlreadyFriend = currentFriendIds.contains(id);

                                    UserModel user = new UserModel(id, username, (int) elo, profilePicture, isAlreadyFriend);
                                    searchResultsList.add(user);
                                }
                            }

                            searchResultsAdapter.notifyDataSetChanged();

                            friendsRecyclerView.setVisibility(View.GONE);
                            placeholderText.setVisibility(View.GONE);
                            searchResultsContainer.setVisibility(View.VISIBLE);

                            if (searchResultsList.isEmpty()) {
                                Toast.makeText(getContext(), "Aucun utilisateur trouvé", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Erreur lors de la recherche", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addFriend(String friendId) {
        if (currentFriendIds.contains(friendId)) {
            Toast.makeText(getContext(), "Cet utilisateur est déjà votre ami", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(currentUserId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<String> friendIds = (List<String>) document.get("friends");

                                if (friendIds == null) {
                                    friendIds = new ArrayList<>();
                                }

                                if (!friendIds.contains(friendId)) {
                                    friendIds.add(friendId);

                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("friends", friendIds);

                                    List<String> finalFriendIds = friendIds;
                                    db.collection("users").document(currentUserId)
                                            .update(updates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        currentFriendIds = new ArrayList<>(finalFriendIds);

                                                        addCurrentUserToFriendsList(friendId);
                                                    } else {
                                                        Toast.makeText(getContext(), "Échec de l'ajout d'ami", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(getContext(), "Cet utilisateur est déjà votre ami", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
    }

    private void addCurrentUserToFriendsList(String friendId) {
        db.collection("users").document(friendId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<String> friendIds = (List<String>) document.get("friends");

                                if (friendIds == null) {
                                    friendIds = new ArrayList<>();
                                }

                                if (!friendIds.contains(currentUserId)) {  // Vérification supplémentaire
                                    friendIds.add(currentUserId);

                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("friends", friendIds);

                                    db.collection("users").document(friendId)
                                            .update(updates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getContext(), "Ami ajouté avec succès", Toast.LENGTH_SHORT).show();
                                                        loadFriendsList();
                                                        searchEditText.setText("");
                                                        searchResultsContainer.setVisibility(View.GONE);
                                                    } else {
                                                        Toast.makeText(getContext(), "Échec de l'ajout d'ami (relation inverse)", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(getContext(), "Ami ajouté avec succès", Toast.LENGTH_SHORT).show();
                                    loadFriendsList();
                                    searchEditText.setText("");
                                    searchResultsContainer.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                });
    }

    private class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

        private List<UserModel> friendsList;

        public FriendsAdapter(List<UserModel> friendsList) {
            this.friendsList = friendsList;
        }

        @NonNull
        @Override
        public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
            return new FriendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
            UserModel friend = friendsList.get(position);
            holder.bind(friend);
        }

        @Override
        public int getItemCount() {
            return friendsList.size();
        }

        class FriendViewHolder extends RecyclerView.ViewHolder {

            private ImageView profileImageView;
            private TextView usernameTextView;
            private Button challengeButton;

            public FriendViewHolder(@NonNull View itemView) {
                super(itemView);
                profileImageView = itemView.findViewById(R.id.profile_image_view);
                usernameTextView = itemView.findViewById(R.id.username_text_view);
                challengeButton = itemView.findViewById(R.id.challenge_button);
            }

            public void bind(UserModel friend) {
                usernameTextView.setText(friend.getUsername());

                if (friend.getProfilePicture() != null && !friend.getProfilePicture().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(friend.getProfilePicture())
                            .placeholder(R.drawable.profile_picture_placeholder)
                            .into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.profile_picture_placeholder);
                }

                challengeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(), "Défi envoyé à " + friend.getUsername(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder> {

        private List<UserModel> searchResultsList;

        public SearchResultsAdapter(List<UserModel> searchResultsList) {
            this.searchResultsList = searchResultsList;
        }

        @NonNull
        @Override
        public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
            return new SearchResultViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
            UserModel user = searchResultsList.get(position);
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return searchResultsList.size();
        }

        class SearchResultViewHolder extends RecyclerView.ViewHolder {

            private ImageView profileImageView;
            private TextView usernameTextView;
            private TextView eloTextView;
            private Button addFriendButton;

            public SearchResultViewHolder(@NonNull View itemView) {
                super(itemView);
                profileImageView = itemView.findViewById(R.id.profile_image_view);
                usernameTextView = itemView.findViewById(R.id.username_text_view);
                eloTextView = itemView.findViewById(R.id.elo_text_view);
                addFriendButton = itemView.findViewById(R.id.add_friend_button);
            }

            public void bind(final UserModel user) {
                usernameTextView.setText(user.getUsername());
                eloTextView.setText("Elo: " + user.getElo());

                if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(user.getProfilePicture())
                            .placeholder(R.drawable.profile_picture_placeholder)
                            .into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.profile_picture_placeholder);
                }

                if (user.isAlreadyFriend()) {
                    addFriendButton.setVisibility(View.GONE);
                } else {
                    addFriendButton.setVisibility(View.VISIBLE);
                    addFriendButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            addFriend(user.getId());
                        }
                    });
                }
            }
        }
    }

    public static class UserModel {
        private String id;
        private String username;
        private int elo;
        private String profilePicture;
        private boolean isAlreadyFriend;

        public UserModel(String id, String username, int elo, String profilePicture) {
            this(id, username, elo, profilePicture, false);
        }

        public UserModel(String id, String username, int elo, String profilePicture, boolean isAlreadyFriend) {
            this.id = id;
            this.username = username;
            this.elo = elo;
            this.profilePicture = profilePicture;
            this.isAlreadyFriend = isAlreadyFriend;
        }

        public String getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public int getElo() {
            return elo;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public boolean isAlreadyFriend() {
            return isAlreadyFriend;
        }
    }
}