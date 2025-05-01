package com.example.smartchess.chess.gamemodes;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.Move;
import com.example.smartchess.chess.chessboard.pieces.Piece;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MultiplayerGameMode implements GameMode {

    private final DatabaseReference gamesRef;
    private final String gameId;
    private final String playerColor;

    public MultiplayerGameMode(String gameId, String playerColor) {
        this.gamesRef = FirebaseDatabase.getInstance().getReference("games");
        this.gameId = gameId;
        this.playerColor = playerColor;
    }

    @Override
    public void onMoveValidated(Move move, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        System.out.println("Move validated: " + move.getToRow() + ", " + move.getToCol());

        gamesRef.child(gameId).child("moves").push().setValue(move)
                .addOnSuccessListener(unused -> Log.d("Matchmaker", "Coup envoyé avec succès !"))
                .addOnFailureListener(e -> Log.e("Matchmaker", "Erreur lors de l'envoi du coup", e));

        view.invalidate();
        onTurnChanged(playerColor.equals("white"), game, view, playerInfoViewWhite, playerInfoViewBlack);
    }

    @Override
    public void onTurnChanged(boolean whiteTurn, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        getCurrentTurn(turn -> {
            game.setWhiteTurn(!turn.equals("white"));

            gamesRef.child(gameId).child("turn").setValue(turn.equals("white") ? "black" : "white")
                    .addOnSuccessListener(unused -> Log.d("Matchmaker", "Tour changé avec succès !"))
                    .addOnFailureListener(e -> Log.e("Matchmaker", "Erreur lors du changement de tour", e));
        });

        view.setSelectedCol(-1);
        view.setSelectedRow(-1);
    }

    @Override
    public void onGameOver(String winner, String description) {
        System.out.println("Game over: " + winner + " wins! " + description);
        gamesRef.child(gameId).removeValue()
                .addOnSuccessListener(unused -> Log.d("Matchmaker", "Partie terminée !"))
                .addOnFailureListener(e -> Log.e("Matchmaker", "Erreur lors de la suppression de la partie", e));
    }

    @Override
    public void beforeMovePiece(ChessGame game) {
        getCurrentTurn(turn -> {
            if (turn.equals("white")) {
                game.setWhiteTurn(true);
            } else {
                game.setWhiteTurn(false);
            }
        });
    }

    @Override
    public void initGame(ChessGame game, ChessBoardView view) {
        if(playerColor.equals("black")) {
            game.setBoardOrientation(ChessGame.BoardOrientation.BLACK);
            view.setBoardOrientation(ChessGame.BoardOrientation.BLACK);
        }

        startListeningForMoves(game, view);
    }

    public void validateMove(Move move, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        checkIfMyTurn(isMyTurn -> {
            if (!isMyTurn) {
                System.out.println("Ce n'est pas votre tour !");
                return;
            }

            if(!move.getColor().equals(playerColor)){
                System.out.println("Ce n'est pas votre couleur !");
                return;
            }

            onMoveValidated(move, game, view, playerInfoViewWhite, playerInfoViewBlack);
        });
    }

    public void checkIfMyTurn(OnTurnCheckListener listener) {
        gamesRef.child(gameId).child("turn").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentTurn = snapshot.getValue(String.class);
                boolean isMyTurn = playerColor.equals(currentTurn);
                listener.onCheck(isMyTurn);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Matchmaker", "Erreur lors de la vérification du tour", error.toException());
                listener.onCheck(false);
            }
        });
    }

    public void getCurrentTurn(OnCurrentTurnListener listener) {
        gamesRef.child(gameId).child("turn").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentTurn = snapshot.getValue(String.class);
                listener.onResult(currentTurn != null ? currentTurn : "unknown");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Matchmaker", "Erreur lors de la récupération du tour actuel", error.toException());
                listener.onResult("error");
            }
        });
    }

    public void startListeningForMoves(ChessGame game, ChessBoardView view) {
        gamesRef.child(gameId).child("moves").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                System.out.println("Coup reçu");
                Move move = snapshot.getValue(Move.class);
                if (move != null) {
                    System.out.println("Coup reçu : " + move.getFromRow() + "," + move.getFromCol() + " -> " + move.getToRow() + "," + move.getToCol());

                    if(!playerColor.equals(move.getColor())){
                        Piece pieceToMove = game.getPiece(move.getFromRow(), move.getFromCol());
                        if (pieceToMove != null) {
                            view.animateMove(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol(), pieceToMove);

                            view.setAnimationEndCallback(new ChessBoardView.AnimationEndCallback() {
                                @Override
                                public void onAnimationEnd() {
                                    boolean moveOk = game.movePiece(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol());

                                    getCurrentTurn(turn -> {
                                        game.setWhiteTurn(turn.equals("white"));
                                    });
                                }
                            });
                        }
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Multiplayer", "Erreur lors de l'écoute des coups", error.toException());
            }
        });
    }

    public interface OnTurnCheckListener {
        void onCheck(boolean isMyTurn);
    }

    public interface OnCurrentTurnListener {
        void onResult(String currentTurn);
    }
}
