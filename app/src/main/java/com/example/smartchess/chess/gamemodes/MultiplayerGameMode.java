package com.example.smartchess.chess.gamemodes;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.Move;
import com.example.smartchess.chess.chessboard.Position;
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


        // Envoyer le coup au serveur
        gamesRef.child(gameId).child("moves").push().setValue(move)
                .addOnSuccessListener(unused -> Log.d("Matchmaker", "Coup envoyé avec succès !"))
                .addOnFailureListener(e -> Log.e("Matchmaker", "Erreur lors de l'envoi du coup", e));

        view.invalidate();

        // changer le tour
        onTurnChanged(playerColor.equals("white"), game, view, null, null);

    }

    @Override
    public void onTurnChanged(boolean whiteTurn, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        // Changer le tour dans le serveur
        getCurrentTurn(turn -> {

            // Changer le tour dans le jeu
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

        //actualiser le tour en fonction des données serveur
        getCurrentTurn(turn -> {
            if (turn.equals("white")) {
                System.out.println("C'est le tour des blancs");
                game.setWhiteTurn(true);
            } else {
                System.out.println("C'est le tour des noirs");
                game.setWhiteTurn(false);
            }
        });
    }


    @Override
    public void initGame(ChessGame game, ChessBoardView view) {

        //si je suis le joueur noir je dois tourner le plateau
        if(playerColor.equals("black")) {
            game.setBoardOrientation(ChessGame.BoardOrientation.BLACK);
            view.setBoardOrientation(ChessGame.BoardOrientation.BLACK);
        }

        startListeningForMoves(game, view);

    }

    /**
     * Vérifie si le coup est valide et l’appliquer.
     */
    public void validateMove(Move move, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        checkIfMyTurn(isMyTurn -> {
            if (!isMyTurn) {
                System.out.println("Ce n'est pas votre tour !");
                return;
            }

            //verifier si je bouge ma couleur
            if(!move.getColor().equals(playerColor)){
                System.out.println("Ce n'est pas votre couleur !");
                return;
            }

            boolean moveOk = game.canMovePiece(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol());
            if (moveOk) {
                //appliquer animation

                //animate
                animateMove(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol(),game.getPiece(move.getFromRow(), move.getFromCol()), view);
                view.setAnimationEndCallback(() -> {

                    boolean moveEffectueOk = game.movePiece(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol());
                    onMoveValidated(move, game, view, playerInfoViewWhite, playerInfoViewBlack);


                });


            } else {
                System.out.println("Coup invalide !");
            }
        });
    }

    public void animateMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece, ChessBoardView boardView) {

        boardView.animateMove(fromRow,fromCol,toRow,toCol, piece);

    }



    /**
     * Vérifie si c’est le tour du joueur et retourne le résultat dans un callback.
     */
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


    /**
     * Écoute les nouveaux coups joués par l’adversaire et les applique.
     */
    public void startListeningForMoves(ChessGame game, ChessBoardView view) {
        gamesRef.child(gameId).child("moves").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                System.out.println("Coup reçu");
                System.out.println("Coup reçu : " + snapshot.getValue());
                System.out.println("Nouveau coup reçu : " + snapshot.getValue(Move.class));
                Move move = snapshot.getValue(Move.class);
                if (move != null) {
                    System.out.println("COUP RECU 2");

                    if(!playerColor.equals(move.getColor())){ //si je ne suis pas celui qui a joué le coup
                        System.out.println("COUP RECU 3");

                        boolean moveOk = game.movePiece(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol());

                        view.invalidate();
                        // Changer le tour
                        getCurrentTurn(turn -> {

                            // Changer le tour dans le jeu
                            game.setWhiteTurn(turn.equals("white"));

                        });

                        Log.d("Multiplayer", "Coup reçu et appliqué : " + move + " (OK: " + moveOk + ")");
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

    /**
     * Interface fonctionnelle pour vérifier le tour.
     */
    public interface OnTurnCheckListener {
        void onCheck(boolean isMyTurn);
    }
    public interface OnCurrentTurnListener {
        void onResult(String currentTurn);
    }
}