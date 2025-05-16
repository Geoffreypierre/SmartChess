package com.example.smartchess.home;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartchess.R;
import com.example.smartchess.auth.UserSession;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";
    private RecyclerView recyclerViewOngoing;
    private RecyclerView recyclerViewCompleted;
    private OngoingGamesAdapter ongoingGamesAdapter;
    private CompletedGamesAdapter completedGamesAdapter;
    private TextView txtPlaceholder;
    private String currentUserId;
    private Map<String, UserData> userCache = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        UserSession session = new UserSession(requireContext());
        currentUserId = session.getUserId();

        Log.d(TAG, "Current user ID: " + currentUserId);

        txtPlaceholder = view.findViewById(R.id.txt_placeholder);

        recyclerViewOngoing = view.findViewById(R.id.recycler_view_ongoing);
        recyclerViewOngoing.setLayoutManager(new LinearLayoutManager(getContext()));
        ongoingGamesAdapter = new OngoingGamesAdapter(new ArrayList<>());
        recyclerViewOngoing.setAdapter(ongoingGamesAdapter);

        recyclerViewCompleted = view.findViewById(R.id.recycler_view_completed);
        recyclerViewCompleted.setLayoutManager(new LinearLayoutManager(getContext()));
        completedGamesAdapter = new CompletedGamesAdapter(new ArrayList<>());
        recyclerViewCompleted.setAdapter(completedGamesAdapter);

        getUserData(currentUserId, () -> {
            loadHistoryData();
        });

        return view;
    }

    private void loadHistoryData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("HistoriquePartie")
                .whereEqualTo("result", "En cours")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<GameHistoryItem> ongoingGames = new ArrayList<>();

                    if (queryDocumentSnapshots.isEmpty()) {
                        ongoingGamesAdapter.updateData(ongoingGames);
                        updatePlaceholderVisibility();
                        return;
                    }

                    AtomicInteger totalGamesToProcess = new AtomicInteger(0);
                    AtomicInteger processedGames = new AtomicInteger(0);

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String id = document.getId();
                            String playerId = document.getString("playerId");
                            String player1Id = document.getString("player1Id");
                            String player2Id = document.getString("player2Id");
                            String result = document.getString("result");
                            String winnerId = document.getString("winnerId");
                            Object timestamp = document.get("timestamp");
                            List<String> moves = (List<String>) document.get("moves");
                            Object duration = document.get("duration");

                            GameHistoryItem item = new GameHistoryItem();
                            item.setGameId(id);

                            if (playerId == null && player1Id != null) {
                                item.setPlayerId(player1Id);
                            } else {
                                item.setPlayerId(playerId);
                            }

                            item.setPlayer2Id(player2Id);
                            item.setResult(result);
                            item.setWinnerId(winnerId);
                            item.setTimestamp(timestamp);
                            item.setMoves(moves);
                            item.setDuration(duration);

                            String effectivePlayerId = (playerId != null) ? playerId : player1Id;
                            if (currentUserId.equals(effectivePlayerId) ||
                                    currentUserId.equals(player2Id)) {
                                totalGamesToProcess.incrementAndGet();
                                ongoingGames.add(item);
                                Log.d(TAG, "Added ongoing game: " + id + " between players " +
                                        effectivePlayerId + " and " + player2Id);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing ongoing game document", e);
                        }
                    }

                    if (totalGamesToProcess.get() == 0) {
                        ongoingGamesAdapter.updateData(ongoingGames);
                        updatePlaceholderVisibility();
                        return;
                    }

                    for (GameHistoryItem game : ongoingGames) {
                        getUserData(game.getPlayerId(), () -> {
                            getUserData(game.getPlayer2Id(), () -> {
                                if (processedGames.incrementAndGet() == totalGamesToProcess.get()) {
                                    ongoingGamesAdapter.updateData(ongoingGames);
                                    updatePlaceholderVisibility();
                                }
                            });
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading ongoing games", e);
                    updatePlaceholderVisibility();
                });

        db.collection("HistoriquePartie")
                .whereNotEqualTo("result", "En cours")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<GameHistoryItem> completedGames = new ArrayList<>();

                    if (queryDocumentSnapshots.isEmpty()) {
                        completedGamesAdapter.updateData(completedGames);
                        updatePlaceholderVisibility();
                        return;
                    }

                    AtomicInteger totalGamesToProcess = new AtomicInteger(0);
                    AtomicInteger processedGames = new AtomicInteger(0);

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String id = document.getId();
                            String playerId = document.getString("playerId");
                            String player1Id = document.getString("player1Id");
                            String player2Id = document.getString("player2Id");
                            String result = document.getString("result");
                            String winnerId = document.getString("winnerId");
                            Object timestamp = document.get("timestamp");
                            List<String> moves = (List<String>) document.get("moves");
                            Object duration = document.get("duration");

                            GameHistoryItem item = new GameHistoryItem();
                            item.setGameId(id);

                            if (playerId == null && player1Id != null) {
                                item.setPlayerId(player1Id);
                            } else {
                                item.setPlayerId(playerId);
                            }

                            item.setPlayer2Id(player2Id);
                            item.setResult(result);
                            item.setWinnerId(winnerId);
                            item.setTimestamp(timestamp);
                            item.setMoves(moves);
                            item.setDuration(duration);

                            String effectivePlayerId = (playerId != null) ? playerId : player1Id;
                            if (currentUserId.equals(effectivePlayerId) ||
                                    currentUserId.equals(player2Id)) {
                                totalGamesToProcess.incrementAndGet();
                                completedGames.add(item);
                                Log.d(TAG, "Added completed game: " + id + " between players " +
                                        effectivePlayerId + " and " + player2Id);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing completed game document", e);
                        }
                    }

                    if (totalGamesToProcess.get() == 0) {
                        completedGamesAdapter.updateData(completedGames);
                        updatePlaceholderVisibility();
                        return;
                    }

                    for (GameHistoryItem game : completedGames) {
                        getUserData(game.getPlayerId(), () -> {
                            getUserData(game.getPlayer2Id(), () -> {
                                if (processedGames.incrementAndGet() == totalGamesToProcess.get()) {
                                    completedGamesAdapter.updateData(completedGames);
                                    updatePlaceholderVisibility();
                                }
                            });
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading completed games", e);
                    updatePlaceholderVisibility();
                });
    }

    private void updatePlaceholderVisibility() {
        if (ongoingGamesAdapter.getItemCount() == 0 && completedGamesAdapter.getItemCount() == 0) {
            txtPlaceholder.setVisibility(View.VISIBLE);
        } else {
            txtPlaceholder.setVisibility(View.GONE);
        }
    }

    private static class UserData {
        private String username;
        private long elo;
        private String avatarUrl;

        public UserData(String username, long elo, String avatarUrl) {
            this.username = username;
            this.elo = elo;
            this.avatarUrl = avatarUrl;
        }

        public String getUsername() {
            return username;
        }

        public long getElo() {
            return elo;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }
    }

    private void getUserData(String userId, Runnable onComplete) {
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "getUserData called with null or empty userId");
            if (onComplete != null) onComplete.run();
            return;
        }

        if (userCache.containsKey(userId)) {
            if (onComplete != null) onComplete.run();
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        Long elo = documentSnapshot.getLong("elo");
                        String avatarUrl = documentSnapshot.getString("avatarUrl");

                        if (username == null) username = "Joueur";
                        if (elo == null) elo = 0L;

                        if (avatarUrl == null || avatarUrl.isEmpty()) {
                            String potentialImagePath = "gs://a071584826.firebasestorage.app/profile_images/" + userId + ".jpg";
                            Log.d(TAG, "No avatarUrl found, trying default path: " + potentialImagePath);

                            StorageReference storageRef = FirebaseStorage.getInstance()
                                    .getReferenceFromUrl(potentialImagePath);
                            String finalUsername = username;
                            Long finalElo = elo;
                            storageRef.getMetadata()
                                    .addOnSuccessListener(metadata -> {
                                        userCache.put(userId, new UserData(finalUsername, finalElo, potentialImagePath));
                                        Log.d(TAG, "Found avatar by userId: " + potentialImagePath);
                                        if (onComplete != null) onComplete.run();
                                    })
                                    .addOnFailureListener(e -> {
                                        userCache.put(userId, new UserData(finalUsername, finalElo, null));
                                        Log.d(TAG, "No avatar found for user: " + userId);
                                        if (onComplete != null) onComplete.run();
                                    });
                        } else {
                            userCache.put(userId, new UserData(username, elo, avatarUrl));
                            Log.d(TAG, "User data loaded for " + userId + ": " + username + ", avatar: " + avatarUrl);
                            if (onComplete != null) onComplete.run();
                        }
                    } else {
                        userCache.put(userId, new UserData("Inconnu", 0, null));
                        Log.d(TAG, "User not found: " + userId);
                        if (onComplete != null) onComplete.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user details for " + userId, e);
                    userCache.put(userId, new UserData("Erreur", 0, null));
                    if (onComplete != null) onComplete.run();
                });
    }

    private class OngoingGamesAdapter extends RecyclerView.Adapter<OngoingGamesAdapter.OngoingGameViewHolder> {
        private List<GameHistoryItem> games;

        public OngoingGamesAdapter(List<GameHistoryItem> games) {
            this.games = games;
        }

        @NonNull
        @Override
        public OngoingGameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ongoing_game, parent, false);
            return new OngoingGameViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OngoingGameViewHolder holder, int position) {
            GameHistoryItem game = games.get(position);
            holder.bind(game);
        }

        @Override
        public int getItemCount() {
            return games.size();
        }

        public void updateData(List<GameHistoryItem> newGames) {
            this.games = newGames;
            notifyDataSetChanged();
        }

        class OngoingGameViewHolder extends RecyclerView.ViewHolder {
            TextView txtLeftPlayer, txtRightPlayer;
            TextView txtLeftElo, txtRightElo;
            TextView btnResume;
            ImageView leftPlayerIcon, rightPlayerIcon;

            OngoingGameViewHolder(@NonNull View itemView) {
                super(itemView);
                txtLeftPlayer = itemView.findViewById(R.id.txt_left_player);
                txtRightPlayer = itemView.findViewById(R.id.txt_right_player);
                txtLeftElo = itemView.findViewById(R.id.txt_left_elo);
                txtRightElo = itemView.findViewById(R.id.txt_right_elo);
                btnResume = itemView.findViewById(R.id.btn_resume);
                leftPlayerIcon = itemView.findViewById(R.id.left_player_icon);
                rightPlayerIcon = itemView.findViewById(R.id.right_player_icon);
            }

            void bind(GameHistoryItem game) {
                boolean isPlayer1 = currentUserId.equals(game.getPlayerId());
                String playerColor = isPlayer1 ? "white" : "black";

                String currentPlayerId = currentUserId;
                String opponentId = isPlayer1 ? game.getPlayer2Id() : game.getPlayerId();

                UserData currentUserData = userCache.get(currentPlayerId);
                if (currentUserData != null) {
                    txtLeftPlayer.setText(currentUserData.getUsername());
                    txtLeftElo.setText("(" + currentUserData.getElo() + ")");

                    if (currentUserData.getAvatarUrl() != null && !currentUserData.getAvatarUrl().isEmpty()) {
                        loadProfileImage(currentUserData.getAvatarUrl(), leftPlayerIcon);
                    } else {
                        leftPlayerIcon.setImageResource(R.drawable.profile_picture_placeholder);
                    }
                } else {
                    txtLeftPlayer.setText("Vous");
                    txtLeftElo.setText("(...)");
                    leftPlayerIcon.setImageResource(R.drawable.profile_picture_placeholder);
                }

                UserData opponentData = userCache.get(opponentId);
                if (opponentData != null) {
                    txtRightPlayer.setText(opponentData.getUsername());
                    txtRightElo.setText("(" + opponentData.getElo() + ")");

                    if (opponentData.getAvatarUrl() != null && !opponentData.getAvatarUrl().isEmpty()) {
                        loadProfileImage(opponentData.getAvatarUrl(), rightPlayerIcon);
                    } else {
                        rightPlayerIcon.setImageResource(R.drawable.profile_picture_placeholder);
                    }
                } else {
                    txtRightPlayer.setText("Adversaire");
                    txtRightElo.setText("(...)");
                    rightPlayerIcon.setImageResource(R.drawable.profile_picture_placeholder);
                }

                btnResume.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), ChessGameActivity.class);
                    intent.putExtra("game_mode", "multiplayer");
                    intent.putExtra("game_id", game.getGameId());
                    intent.putExtra("player_color", playerColor);
                    intent.putExtra("playerWhiteId", game.getPlayerId());
                    intent.putExtra("playerBlackId", game.getPlayer2Id());
                    startActivity(intent);
                });
            }
        }
    }

    private class CompletedGamesAdapter extends RecyclerView.Adapter<CompletedGamesAdapter.CompletedGameViewHolder> {
        private List<GameHistoryItem> games;

        public CompletedGamesAdapter(List<GameHistoryItem> games) {
            this.games = games;
        }

        @NonNull
        @Override
        public CompletedGameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_completed_game, parent, false);
            return new CompletedGameViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CompletedGameViewHolder holder, int position) {
            GameHistoryItem game = games.get(position);
            holder.bind(game);
        }

        @Override
        public int getItemCount() {
            return games.size();
        }

        public void updateData(List<GameHistoryItem> newGames) {
            this.games = newGames;
            notifyDataSetChanged();
        }

        class CompletedGameViewHolder extends RecyclerView.ViewHolder {
            TextView txtLeftPlayer, txtRightPlayer;
            TextView txtLeftElo, txtRightElo;
            TextView txtLeftScore, txtRightScore;
            ImageView resultIcon, leftPlayerIcon, rightPlayerIcon;

            CompletedGameViewHolder(@NonNull View itemView) {
                super(itemView);
                txtLeftPlayer = itemView.findViewById(R.id.txt_left_player);
                txtRightPlayer = itemView.findViewById(R.id.txt_right_player);
                txtLeftElo = itemView.findViewById(R.id.txt_left_elo);
                txtRightElo = itemView.findViewById(R.id.txt_right_elo);
                txtLeftScore = itemView.findViewById(R.id.txt_left_score);
                txtRightScore = itemView.findViewById(R.id.txt_right_score);
                resultIcon = itemView.findViewById(R.id.result_icon);
                leftPlayerIcon = itemView.findViewById(R.id.left_player_icon);
                rightPlayerIcon = itemView.findViewById(R.id.right_player_icon);
            }

            void bind(GameHistoryItem game) {
                boolean isPlayer1 = currentUserId.equals(game.getPlayerId());
                boolean isWinner = currentUserId.equals(game.getWinnerId());

                String currentPlayerId = currentUserId;
                String opponentId = isPlayer1 ? game.getPlayer2Id() : game.getPlayerId();

                UserData currentUserData = userCache.get(currentPlayerId);
                if (currentUserData != null) {
                    txtLeftPlayer.setText(currentUserData.getUsername());
                    txtLeftElo.setText("(" + currentUserData.getElo() + ")");

                    if (currentUserData.getAvatarUrl() != null && !currentUserData.getAvatarUrl().isEmpty()) {
                        loadProfileImage(currentUserData.getAvatarUrl(), leftPlayerIcon);
                    } else {
                        leftPlayerIcon.setImageResource(R.drawable.profile_picture_placeholder);
                    }
                } else {
                    txtLeftPlayer.setText("Vous");
                    txtLeftElo.setText("(...)");
                    leftPlayerIcon.setImageResource(R.drawable.profile_picture_placeholder);
                }

                UserData opponentData = userCache.get(opponentId);
                if (opponentData != null) {
                    txtRightPlayer.setText(opponentData.getUsername());
                    txtRightElo.setText("(" + opponentData.getElo() + ")");

                    if (opponentData.getAvatarUrl() != null && !opponentData.getAvatarUrl().isEmpty()) {
                        loadProfileImage(opponentData.getAvatarUrl(), rightPlayerIcon);
                    } else {
                        rightPlayerIcon.setImageResource(R.drawable.profile_picture_placeholder);
                    }
                } else {
                    txtRightPlayer.setText("Adversaire");
                    txtRightElo.setText("(...)");
                    rightPlayerIcon.setImageResource(R.drawable.profile_picture_placeholder);
                }

                if ("Abandon".equals(game.getResult()) || "Checkmate".equals(game.getResult()) ||
                        "Échec et mat".equals(game.getResult())) {
                    if (isWinner) {
                        txtLeftScore.setText("1");
                        txtRightScore.setText("0");
                        resultIcon.setImageResource(R.drawable.victory_icon);
                    } else {
                        txtLeftScore.setText("0");
                        txtRightScore.setText("1");
                        resultIcon.setImageResource(R.drawable.defeat_icon);
                    }
                } else if ("Draw".equals(game.getResult()) || "Pat".equals(game.getResult()) ||
                        "Nulle".equals(game.getResult())) {
                    txtLeftScore.setText("½");
                    txtRightScore.setText("½");
                    resultIcon.setImageResource(R.drawable.null_icon);
                } else {
                    if (isWinner) {
                        txtLeftScore.setText("1");
                        txtRightScore.setText("0");
                        resultIcon.setImageResource(R.drawable.victory_icon);
                    } else {
                        txtLeftScore.setText("0");
                        txtRightScore.setText("1");
                        resultIcon.setImageResource(R.drawable.defeat_icon);
                    }
                }
                itemView.setOnClickListener(v -> {
                    showMoveHistoryDialog(game);
                });
            }
        }
    }

    private void showMoveHistoryDialog(GameHistoryItem game) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_move_history);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageButton btnClose = dialog.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        RecyclerView recyclerView = dialog.findViewById(R.id.recycler_view_moves);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<String> moves = game.getMoves();
        if (moves == null) moves = new ArrayList<>();

        MoveHistoryAdapter adapter = new MoveHistoryAdapter(moves);
        recyclerView.setAdapter(adapter);

        dialog.show();
    }

    private void loadProfileImage(String avatarUrl, ImageView imageView) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.profile_picture_placeholder);
            return;
        }

        try {
            if (avatarUrl.startsWith("gs://")) {
                Log.d(TAG, "Loading image from Firebase Storage: " + avatarUrl);
                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(avatarUrl);
                storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            if (getContext() != null) {
                                Glide.with(getContext())
                                        .load(uri)
                                        .error(R.drawable.profile_picture_placeholder)
                                        .into(imageView);
                                Log.d(TAG, "Image loaded successfully from: " + uri);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to load profile image from: " + avatarUrl, e);
                            if (!avatarUrl.contains("profile_images")) {
                                String filename = getFilenameFromGsUrl(avatarUrl);
                                String newUrl = "gs://a071584826.firebasestorage.app/profile_images/" + filename;
                                Log.d(TAG, "Trying alternative path: " + newUrl);
                                loadProfileImage(newUrl, imageView);
                            } else {
                                imageView.setImageResource(R.drawable.profile_picture_placeholder);
                            }
                        });
            }
            else if (avatarUrl.startsWith("http")) {
                if (getContext() != null) {
                    Glide.with(getContext())
                            .load(avatarUrl)
                            .error(R.drawable.profile_picture_placeholder)
                            .into(imageView);
                    Log.d(TAG, "Loading http image: " + avatarUrl);
                }
            }
            else {
                String storageUrl = "gs://a071584826.firebasestorage.app/profile_images/" + avatarUrl;
                Log.d(TAG, "Converting relative path to full path: " + storageUrl);
                loadProfileImage(storageUrl, imageView);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing profile image URL: " + avatarUrl, e);
            imageView.setImageResource(R.drawable.profile_picture_placeholder);
        }
    }

    private String getFilenameFromGsUrl(String gsUrl) {
        if (gsUrl == null || gsUrl.isEmpty()) {
            return "";
        }

        String[] parts = gsUrl.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return "";
    }

    public static class GameHistoryItem {
        private String gameId;
        private String playerId;
        private String player1Id;
        private String player2Id;
        private String result;
        private String winnerId;
        private Object timestamp;
        private List<String> moves;
        private Object duration;

        public GameHistoryItem() {
        }

        public String getGameId() {
            return gameId;
        }

        public void setGameId(String gameId) {
            this.gameId = gameId;
        }

        public String getPlayerId() {
            return playerId;
        }

        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }

        public String getPlayer1Id() {
            return player1Id;
        }

        public void setPlayer1Id(String player1Id) {
            this.player1Id = player1Id;
            if (this.playerId == null) {
                this.playerId = player1Id;
            }
        }

        public String getPlayer2Id() {
            return player2Id;
        }

        public void setPlayer2Id(String player2Id) {
            this.player2Id = player2Id;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getWinnerId() {
            return winnerId;
        }

        public void setWinnerId(String winnerId) {
            this.winnerId = winnerId;
        }

        public long getTimestamp() {
            if (timestamp instanceof Long) {
                return (Long) timestamp;
            } else if (timestamp instanceof com.google.firebase.Timestamp) {
                return ((com.google.firebase.Timestamp) timestamp).getSeconds() * 1000;
            }
            return 0;
        }

        public void setTimestamp(Object timestamp) {
            this.timestamp = timestamp;
        }

        public List<String> getMoves() {
            return moves;
        }

        public void setMoves(List<String> moves) {
            this.moves = moves;
        }

        public long getDuration() {
            if (duration instanceof Long) {
                return (Long) duration;
            } else if (duration instanceof Number) {
                return ((Number) duration).longValue();
            }
            return 0;
        }

        public void setDuration(Object duration) {
            this.duration = duration;
        }
    }
}