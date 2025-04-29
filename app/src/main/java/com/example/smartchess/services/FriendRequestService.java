package com.example.smartchess.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.smartchess.MainActivity;
import com.example.smartchess.R;
import com.example.smartchess.play.FriendRequestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

public class FriendRequestService extends Service {

    private static final String CHANNEL_ID = "friend_requests_channel";
    private static final int NOTIFICATION_ID = 1001;

    private FirebaseFirestore db;
    private String currentUserId;
    private ListenerRegistration requestsListener;

    @Override
    public void onCreate() {
        super.onCreate();

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            createNotificationChannel();
            startListeningForRequests();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (requestsListener != null) {
            requestsListener.remove();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Demandes d'amitié";
            String description = "Notifications pour les nouvelles demandes d'amitié";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void startListeningForRequests() {
        if (currentUserId == null) return;

        Query query = db.collection("friendRequests")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", "pending");

        requestsListener = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                return;
            }

            if (snapshots != null && !snapshots.isEmpty()) {
                for (com.google.firebase.firestore.DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        FriendRequestModel request = dc.getDocument().toObject(FriendRequestModel.class);
                        showNotification(request);
                    }
                }
            }
        });
    }

    private void showNotification(FriendRequestModel request) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("OPEN_FRIENDS_TAB", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.tittle_friends)
                .setContentTitle("Nouvelle demande d'amitié")
                .setContentText(request.getSenderUsername() + " souhaite vous ajouter comme ami")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID + (int) System.currentTimeMillis(), builder.build());
    }
}