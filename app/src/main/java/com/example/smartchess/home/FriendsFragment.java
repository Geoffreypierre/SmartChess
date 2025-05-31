package com.example.smartchess.home;

import android.app.Dialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import android.content.Intent;
import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView friendsRecyclerView;
    private RecyclerView searchResultsRecyclerView;
    private TextView placeholderText;
    private LinearLayout searchResultsContainer;
    private Button pendingRequestsButton;

    private FirebaseFirestore db;
    private String currentUserId;
    private String currentUsername;
    private String currentProfilePic;
    private UserSession userSession;

    private FriendsAdapter friendsAdapter;
    private SearchResultsAdapter searchResultsAdapter;
    private List<UserModel> friendsList;
    private List<UserModel> searchResultsList;
    private List<String> currentFriendIds;
    private List<FriendRequestModel> pendingRequests;

    private DatabaseReference challengeRef;
    private ChildEventListener challengeListener;
    private List<ChallengeModel> receivedChallenges = new ArrayList<>();

    private ChildEventListener sentChallengesListener;
    private DatabaseReference sentChallengesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userSession = new UserSession(getContext());

        loadCurrentUserInfo();

        initViews(view);
        setupRecyclerViews();
        setupListeners();

        loadFriendsList();
        checkForPendingRequests();
        setupChallengeListener();
        setupSentChallengesListener();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (challengeRef != null && challengeListener != null) {
            challengeRef.removeEventListener(challengeListener);
        }

        if (sentChallengesRef != null && sentChallengesListener != null) {
            sentChallengesRef.removeEventListener(sentChallengesListener);
        }
    }

    private void sendChallenge(String targetUserId, String targetUsername) {
        DatabaseReference challengeRef = FirebaseDatabase.getInstance()
                .getReference("challenges")
                .child(targetUserId)
                .push();

        String challengeId = challengeRef.getKey();

        ChallengeModel challenge = new ChallengeModel(
                challengeId,
                currentUserId,
                currentUsername,
                currentProfilePic,
                targetUserId,
                targetUsername,
                System.currentTimeMillis()
        );

        challengeRef.setValue(challenge)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Défi envoyé à " + targetUsername, Toast.LENGTH_SHORT).show();

                    DatabaseReference ourChallengeRef = FirebaseDatabase.getInstance()
                            .getReference("sentChallenges")
                            .child(currentUserId)
                            .child(challengeId);
                    ourChallengeRef.setValue(challenge);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors de l'envoi du défi", Toast.LENGTH_SHORT).show();
                });
    }

    private void showChallengeDialog(ChallengeModel challenge) {
        if (getContext() == null || isDetached()) return;

        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_challenge_received);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView profileImageView = dialog.findViewById(R.id.challenge_profile_image);
        TextView messageTextView = dialog.findViewById(R.id.challenge_message_text);
        Button declineButton = dialog.findViewById(R.id.btn_decline_challenge);
        Button acceptButton = dialog.findViewById(R.id.btn_accept_challenge);

        if (challenge.getChallengerProfilePic() != null && !challenge.getChallengerProfilePic().isEmpty()) {
            Glide.with(getContext())
                    .load(challenge.getChallengerProfilePic())
                    .placeholder(R.drawable.profile_picture_placeholder)
                    .into(profileImageView);
        }

        messageTextView.setText(challenge.getChallengerUsername() + " vous défie à une partie d'échecs !");

        declineButton.setOnClickListener(v -> {
            respondToChallenge(challenge, "declined");
            dialog.dismiss();
        });

        acceptButton.setOnClickListener(v -> {
            respondToChallenge(challenge, "accepted");
            dialog.dismiss();
        });

        dialog.show();
    }

    private void respondToChallenge(ChallengeModel challenge, String response) {
        if (response.equals("accepted")) {
            createMultiplayerGame(challenge);
        } else {
            FirebaseDatabase.getInstance()
                    .getReference("challenges")
                    .child(currentUserId)
                    .child(challenge.getId())
                    .removeValue();

            Toast.makeText(getContext(), "Défi refusé", Toast.LENGTH_SHORT).show();
        }

        receivedChallenges.remove(challenge);
    }

    private void createMultiplayerGame(ChallengeModel challenge) {
        if (getContext() == null || isDetached()) return;

        DocumentReference gameRef = db.collection("multiplayerGames").document();
        String gameId = gameRef.getId();

        Map<String, Object> gameData = new HashMap<>();
        gameData.put("gameId", gameId);
        gameData.put("playerWhiteId", challenge.getChallengerId());
        gameData.put("playerBlackId", currentUserId);
        gameData.put("status", "active");
        gameData.put("createdAt", System.currentTimeMillis());
        gameData.put("moves", new ArrayList<>());

        gameRef.set(gameData)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() == null || isDetached()) return; // Vérification avant Toast

                    FirebaseDatabase.getInstance()
                            .getReference("challenges")
                            .child(currentUserId)
                            .child(challenge.getId())
                            .child("status")
                            .setValue("accepted");

                    FirebaseDatabase.getInstance()
                            .getReference("challenges")
                            .child(currentUserId)
                            .child(challenge.getId())
                            .child("gameId")
                            .setValue(gameId);

                    FirebaseDatabase.getInstance()
                            .getReference("sentChallenges")
                            .child(challenge.getChallengerId())
                            .child(challenge.getId())
                            .child("status")
                            .setValue("accepted");

                    FirebaseDatabase.getInstance()
                            .getReference("sentChallenges")
                            .child(challenge.getChallengerId())
                            .child(challenge.getId())
                            .child("gameId")
                            .setValue(gameId);

                    startChessGame(gameId, "black", challenge.getChallengerId(), currentUserId);
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null && !isDetached()) {
                        Toast.makeText(getContext(), "Erreur lors de la création de la partie", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupSentChallengesListener() {
        sentChallengesRef = FirebaseDatabase.getInstance()
                .getReference("sentChallenges")
                .child(currentUserId);

        sentChallengesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {}

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                if (getContext() == null || isDetached()) return;

                ChallengeModel challenge = dataSnapshot.getValue(ChallengeModel.class);
                if (challenge != null && challenge.getStatus().equals("accepted")) {
                    startMultiplayerGame(challenge);
                    dataSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        sentChallengesRef.addChildEventListener(sentChallengesListener);
    }


    private void startMultiplayerGame(ChallengeModel challenge) {
        startChessGame(challenge.getGameId(), "white", currentUserId, challenge.getTargetId());

        FirebaseDatabase.getInstance()
                .getReference("challenges")
                .child(challenge.getTargetId())
                .child(challenge.getId())
                .removeValue();

        FirebaseDatabase.getInstance()
                .getReference("sentChallenges")
                .child(currentUserId)
                .child(challenge.getId())
                .removeValue();
    }

    private void startChessGame(String gameId, String playerColor, String playerWhiteId, String playerBlackId) {
        if (getContext() == null || getActivity() == null || isDetached()) {
            return;
        }

        Intent intent = new Intent(getContext(), ChessGameActivity.class);
        intent.putExtra("game_mode", "multiplayer");
        intent.putExtra("game_id", gameId);
        intent.putExtra("player_color", playerColor);
        intent.putExtra("playerWhiteId", playerWhiteId);
        intent.putExtra("playerBlackId", playerBlackId);
        startActivity(intent);
    }

    private void setupChallengeListener() {
        challengeRef = FirebaseDatabase.getInstance().getReference("challenges").child(currentUserId);

        challengeListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                if (getContext() == null || isDetached()) return;

                ChallengeModel challenge = dataSnapshot.getValue(ChallengeModel.class);
                if (challenge != null && challenge.getStatus().equals("pending")) {
                    if (challenge.getTargetId().equals(currentUserId)) {
                        receivedChallenges.add(challenge);
                        showChallengeDialog(challenge);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                if (getContext() == null || isDetached()) return;

                ChallengeModel challenge = dataSnapshot.getValue(ChallengeModel.class);
                if (challenge != null && challenge.getStatus().equals("accepted")) {
                    if (challenge.getChallengerId().equals(currentUserId)) {
                        startMultiplayerGame(challenge);
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (getContext() != null && !isDetached()) {
                    Toast.makeText(getContext(), "Erreur de connexion aux défis", Toast.LENGTH_SHORT).show();
                }
            }
        };

        challengeRef.addChildEventListener(challengeListener);
    }

    private void loadCurrentUserInfo() {
        db.collection("users").document(currentUserId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            currentUsername = document.getString("username");
                            currentProfilePic = document.getString("profilePicture");
                        }
                    }
                });
    }

    private void initViews(View view) {
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);
        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view);
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view);
        placeholderText = view.findViewById(R.id.txt_placeholder);
        searchResultsContainer = view.findViewById(R.id.search_results_container);
        pendingRequestsButton = view.findViewById(R.id.pending_requests_button);

        searchResultsContainer.setVisibility(View.GONE);
    }

    private void setupRecyclerViews() {
        friendsList = new ArrayList<>();
        searchResultsList = new ArrayList<>();
        currentFriendIds = new ArrayList<>();
        pendingRequests = new ArrayList<>();

        friendsAdapter = new FriendsAdapter(friendsList);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsRecyclerView.setAdapter(friendsAdapter);

        searchResultsAdapter = new SearchResultsAdapter(searchResultsList);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);
    }

    private void setupListeners() {
        searchButton.setOnClickListener(v -> {
            String searchQuery = searchEditText.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                searchUsers(searchQuery);
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

        pendingRequestsButton.setOnClickListener(v -> {
            showPendingRequests();
        });
    }

    private void loadFriendsList() {
        db.collection("users").document(currentUserId).get()
                .addOnCompleteListener(task -> {
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
                });
    }

    private void loadFriendsDetails(List<String> friendIds) {
        friendsList.clear();

        for (String friendId : friendIds) {
            db.collection("users").document(friendId).get()
                    .addOnCompleteListener(task -> {
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
                    });
        }
    }

    private void searchUsers(String query) {
        searchResultsList.clear();

        db.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();

                            if (!id.equals(currentUserId)) {
                                String username = document.getString("username");
                                long elo = document.getLong("elo") != null ? document.getLong("elo") : 0;
                                String profilePicture = document.getString("profilePicture");

                                boolean isAlreadyFriend = currentFriendIds.contains(id);

                                boolean requestAlreadySent = false;
                                for (FriendRequestModel request : pendingRequests) {
                                    if (request.getReceiverId().equals(id) &&
                                            request.getSenderId().equals(currentUserId) &&
                                            request.getStatus().equals("pending")) {
                                        requestAlreadySent = true;
                                        break;
                                    }
                                }

                                UserModel user = new UserModel(id, username, (int) elo, profilePicture, isAlreadyFriend, requestAlreadySent);
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
                });
    }

    private void sendFriendRequest(String receiverId) {
        DocumentReference requestRef = db.collection("friendRequests").document();
        String requestId = requestRef.getId();

        FriendRequestModel request = new FriendRequestModel(
                requestId,
                currentUserId,
                currentUsername,
                currentProfilePic,
                receiverId
        );

        requestRef.set(request)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Demande d'amitié envoyée", Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < searchResultsList.size(); i++) {
                        if (searchResultsList.get(i).getId().equals(receiverId)) {
                            searchResultsList.get(i).setRequestSent(true);
                            searchResultsAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Échec de l'envoi de la demande", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkForPendingRequests() {
        pendingRequests.clear();
        db.collection("friendRequests")
                .whereEqualTo("senderId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            FriendRequestModel request = document.toObject(FriendRequestModel.class);
                            pendingRequests.add(request);
                        }
                    }
                });

        db.collection("friendRequests")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<FriendRequestModel> receivedRequests = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            FriendRequestModel request = document.toObject(FriendRequestModel.class);
                            receivedRequests.add(request);
                            pendingRequests.add(request);
                        }

                        if (!receivedRequests.isEmpty()) {
                            pendingRequestsButton.setText("Demandes (" + receivedRequests.size() + ")");
                            pendingRequestsButton.setVisibility(View.VISIBLE);
                        } else {
                            pendingRequestsButton.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void showPendingRequests() {
        List<FriendRequestModel> receivedRequests = new ArrayList<>();
        for (FriendRequestModel request : pendingRequests) {
            if (request.getReceiverId().equals(currentUserId) && request.getStatus().equals("pending")) {
                receivedRequests.add(request);
            }
        }

        if (receivedRequests.isEmpty()) {
            Toast.makeText(getContext(), "Aucune demande en attente", Toast.LENGTH_SHORT).show();
            return;
        }

        showFriendRequestDialog(receivedRequests.get(0));
    }

    private void showFriendRequestDialog(FriendRequestModel request) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_friend_request);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView profileImageView = dialog.findViewById(R.id.request_profile_image);
        TextView messageTextView = dialog.findViewById(R.id.request_message_text);
        Button declineButton = dialog.findViewById(R.id.btn_decline);
        Button acceptButton = dialog.findViewById(R.id.btn_accept);

        if (request.getSenderProfilePicture() != null && !request.getSenderProfilePicture().isEmpty()) {
            Glide.with(getContext())
                    .load(request.getSenderProfilePicture())
                    .placeholder(R.drawable.profile_picture_placeholder)
                    .into(profileImageView);
        }

        messageTextView.setText(request.getSenderUsername() + " souhaite vous ajouter comme ami.");

        declineButton.setOnClickListener(v -> {
            respondToFriendRequest(request, "declined");
            dialog.dismiss();
        });

        acceptButton.setOnClickListener(v -> {
            respondToFriendRequest(request, "accepted");
            dialog.dismiss();
        });

        dialog.show();
    }

    private void respondToFriendRequest(FriendRequestModel request, String response) {
        db.collection("friendRequests").document(request.getId())
                .update("status", response)
                .addOnSuccessListener(aVoid -> {
                    if (response.equals("accepted")) {
                            addAsFriends(request.getSenderId(), currentUserId);
                    } else {
                        Toast.makeText(getContext(), "Demande refusée", Toast.LENGTH_SHORT).show();
                    }

                    pendingRequests.remove(request);
                    checkForPendingRequests();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors du traitement de la demande", Toast.LENGTH_SHORT).show();
                });
    }

    private void addAsFriends(String user1Id, String user2Id) {
        db.collection("users").document(user1Id)
                .update("friends", FieldValue.arrayUnion(user2Id))
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(user2Id)
                            .update("friends", FieldValue.arrayUnion(user1Id))
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(getContext(), "Ami ajouté avec succès", Toast.LENGTH_SHORT).show();
                                loadFriendsList();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Erreur lors de l'ajout d'ami", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors de l'ajout d'ami", Toast.LENGTH_SHORT).show();
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

                challengeButton.setOnClickListener(v -> {
                    sendChallenge(friend.getId(), friend.getUsername());
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
                } else if (user.isRequestSent()) {
                    addFriendButton.setText("Demande envoyée");
                    addFriendButton.setEnabled(false);
                    addFriendButton.setVisibility(View.VISIBLE);
                } else {
                    addFriendButton.setText("Ajouter en ami");
                    addFriendButton.setEnabled(true);
                    addFriendButton.setVisibility(View.VISIBLE);
                    addFriendButton.setOnClickListener(v -> {
                        sendFriendRequest(user.getId());
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
        private boolean isRequestSent;

        public UserModel(String id, String username, int elo, String profilePicture) {
            this(id, username, elo, profilePicture, false, false);
        }

        public UserModel(String id, String username, int elo, String profilePicture, boolean isAlreadyFriend) {
            this(id, username, elo, profilePicture, isAlreadyFriend, false);
        }

        public UserModel(String id, String username, int elo, String profilePicture, boolean isAlreadyFriend, boolean isRequestSent) {
            this.id = id;
            this.username = username;
            this.elo = elo;
            this.profilePicture = profilePicture;
            this.isAlreadyFriend = isAlreadyFriend;
            this.isRequestSent = isRequestSent;
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

        public boolean isRequestSent() {
            return isRequestSent;
        }

        public void setRequestSent(boolean requestSent) {
            this.isRequestSent = requestSent;
        }
    }

    public static class ChallengeModel {
        private String id;
        private String challengerId;
        private String challengerUsername;
        private String challengerProfilePic;
        private String targetId;
        private String targetUsername;
        private String status;
        private long timestamp;
        private String gameId;

        public ChallengeModel() {
            this.status = "pending";
        }

        public ChallengeModel(String id, String challengerId, String challengerUsername,
                              String challengerProfilePic, String targetId, String targetUsername, long timestamp) {
            this.id = id;
            this.challengerId = challengerId;
            this.challengerUsername = challengerUsername;
            this.challengerProfilePic = challengerProfilePic;
            this.targetId = targetId;
            this.targetUsername = targetUsername;
            this.status = "pending";
            this.timestamp = timestamp;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getChallengerId() { return challengerId; }
        public void setChallengerId(String challengerId) { this.challengerId = challengerId; }

        public String getChallengerUsername() { return challengerUsername; }
        public void setChallengerUsername(String challengerUsername) { this.challengerUsername = challengerUsername; }

        public String getChallengerProfilePic() { return challengerProfilePic; }
        public void setChallengerProfilePic(String challengerProfilePic) { this.challengerProfilePic = challengerProfilePic; }

        public String getTargetId() { return targetId; }
        public void setTargetId(String targetId) { this.targetId = targetId; }

        public String getTargetUsername() { return targetUsername; }
        public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public String getGameId() { return gameId; }
        public void setGameId(String gameId) { this.gameId = gameId; }
    }
}