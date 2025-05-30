package com.example.smartchess.chess.gamemodes;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.smartchess.auth.ConnexionActivity;
import com.example.smartchess.chess.chessboard.ChessBoardView;
import com.example.smartchess.chess.chessboard.ChessGame;
import com.example.smartchess.chess.chessboard.Move;
import com.example.smartchess.chess.chessboard.Position;
import com.example.smartchess.chess.chessboard.pieces.Piece;
import com.example.smartchess.chess.controller.ChessGameController;
import com.example.smartchess.chess.controller.GameOverInfo;
import com.example.smartchess.chess.historique.HistoriquePartie;
import com.example.smartchess.chess.playerinfos.PlayerInfoView;
import com.example.smartchess.services.HistoriquePartieService;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DiffereGameMode implements GameMode {

    protected ChessGameController.GameOverDialogCallback dialogCallback;

    private final DatabaseReference gamesRef;
    private final String gameId;
    private final String playerColor;
    private boolean processingMove = false;

    public DiffereGameMode(String gameId, String playerColor) {
        this.gamesRef = FirebaseDatabase.getInstance().getReference("differedGames");
        this.gameId = gameId;
        this.playerColor = playerColor;
    }

    @Override
    public void initGame(ChessGame game, ChessBoardView view) {
        if(playerColor.equals("black")) {
            game.setBoardOrientation(ChessGame.BoardOrientation.BLACK);
            view.setBoardOrientation(ChessGame.BoardOrientation.BLACK);
        }
        startListeningForMoves(game, view);
        startListeningForGameOver(game);
        gamesRef.child(gameId).child("boardState").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Map<String, Object> fullState = (Map<String, Object>) snapshot.getValue();
                game.loadFullState(fullState);
                System.out.println("État du plateau restauré !");

                getCurrentTurn(turn -> {
                    game.setWhiteTurn(turn.equals("white"));
                });
                view.invalidate();

            } else {
                System.out.println("Aucun état de plateau trouvé. Utilisation de l'état initial.");
            }
        }).addOnFailureListener(e -> {
            Log.e("DiffereGameMode", "Erreur lors de la récupération du plateau", e);
        });
    }

    @Override
    public void onMoveValidated(Move move, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        System.out.println("Move validated: " + move.getToRow() + ", " + move.getToCol());
        gamesRef.child(gameId).child("moves").push().setValue(move)
                .addOnSuccessListener(unused -> Log.d("Matchmaker", "Coup envoyé avec succès !"))
                .addOnFailureListener(e -> Log.e("Matchmaker", "Erreur lors de l'envoi du coup", e));
        Map<String, Object> state = game.serializeFullState();
        gamesRef.child(gameId).child("boardState").setValue(state);

        view.invalidate();
        onTurnChanged(playerColor.equals("white"), game, view, null, null);
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
    public void onGameOver(String winner, String loser, String description) {
        gamesRef.child(gameId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String playerWhite = snapshot.child("playerWhite").getValue(String.class);
                String playerBlack = snapshot.child("playerBlack").getValue(String.class);

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                DataSnapshot movesSnapshot = snapshot.child("moves");
                List<String> movesList = new ArrayList<>();

                for (DataSnapshot moveSnap : movesSnapshot.getChildren()) {
                    Move move = moveSnap.getValue(Move.class);
                    if (move != null) {
                        String notation = convertMoveToNotation(move);
                        movesList.add(notation);
                    }
                }
                HistoriquePartie partie = new HistoriquePartie();
                partie.setPlayer1Id(playerWhite);
                partie.setPlayer2Id(playerBlack);
                partie.setMoves(movesList);

                if (winner != null && winner.equals("White")){
                    partie.setWinnerId(playerWhite);
                }
                else if (winner != null && winner.equals("Black")){
                    partie.setWinnerId(playerBlack);
                }
                else{
                    if(winner != null){
                        if (winner.equals(playerWhite)){
                            partie.setWinnerId(playerWhite);
                        } else {
                            partie.setWinnerId(playerBlack);
                        }
                    }
                    else if (loser != null){
                        if (loser.equals(playerWhite)){
                            partie.setWinnerId(playerBlack);
                        } else {
                            partie.setWinnerId(playerWhite);
                        }
                    }
                    else{
                        partie.setWinnerId(null);
                    }
                }
                partie.setResult(description);
                partie.setTimestamp(new Date());
                partie.setDuration(120);

                if (dialogCallback != null) {

                    if(partie.getWinnerId() == null){
                        String textpop = "Match nul\n"+description;
                        String eloChange = "+0 Elo";

                        GameOverInfo gameOverInfo = new GameOverInfo(textpop, eloChange);
                        gamesRef.child(gameId).child("gameOver").setValue(gameOverInfo);

                    }
                    else if (partie.getWinnerId().equals(playerWhite)){
                        db.collection("users").document(playerWhite)
                                .get()
                                .addOnCompleteListener(task -> {
                                    String eloChange = "+0 Elo";
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();


                                        if (document.exists()) {
                                            String whitename = document.getString("username");
                                            String textpop = whitename + " a gagné !\n"+description;
                                            dialogCallback.show(textpop,eloChange);

                                            System.out.println("maj 1 elo " + playerWhite + " " + playerBlack + " " + partie.getWinnerId() );
                                            //mettre a jour l'élo des deux joueurs
                                            db.collection("users").document(playerWhite)
                                                    .update("elo", FieldValue.increment(+10));

                                            db.collection("users").document(playerBlack)
                                                    .update("elo", FieldValue.increment(-10));


                                            GameOverInfo gameOverInfo = new GameOverInfo(textpop, eloChange);
                                            gamesRef.child(gameId).child("gameOver").setValue(gameOverInfo);

                                        } else {

                                            String whitename = "Joueur blanc";
                                            String textpop = whitename + " a gagné !\n"+description;

                                            dialogCallback.show(textpop,"+0 Elo");
                                            GameOverInfo gameOverInfo = new GameOverInfo(textpop, eloChange);
                                            gamesRef.child(gameId).child("gameOver").setValue(gameOverInfo);
                                        }
                                    } else {
                                        String whitename = "Joueur blanc";
                                        String textpop = whitename + " a gagné !\n"+description;
                                        dialogCallback.show(textpop,eloChange);
                                        GameOverInfo gameOverInfo = new GameOverInfo(textpop, eloChange);
                                        gamesRef.child(gameId).child("gameOver").setValue(gameOverInfo);
                                    }
                                });

                    }
                    else{
                        db.collection("users").document(playerBlack)
                                .get()
                                .addOnCompleteListener(task -> {

                                    String eloChange = "+0 Elo";

                                    System.out.println("appel firestore");
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        System.out.println("Document : " + document);
                                        System.out.println("Document ID : " + document.getId());
                                        if (document.exists()) {
                                            System.out.println("maj 2 elo" + playerWhite + " " + playerBlack + " " + partie.getWinnerId() );

                                            System.out.println("Document existe");
                                            String blackname = document.getString("username");
                                            String textpop = blackname + " a gagné !\n"+description;
                                            dialogCallback.show(textpop,eloChange);

                                            //mettre a jour l'élo des deux joueurs
                                            db.collection("users").document(playerWhite)
                                                    .update("elo", FieldValue.increment(-10));

                                            db.collection("users").document(playerBlack)
                                                    .update("elo", FieldValue.increment(+10));


                                            GameOverInfo gameOverInfo = new GameOverInfo(textpop, eloChange);
                                            gamesRef.child(gameId).child("gameOver").setValue(gameOverInfo);

                                        } else {

                                            String blackname = "Joueur noir";
                                            String textpop = blackname + " a gagné !\n"+description;
                                            dialogCallback.show(textpop,eloChange);

                                            GameOverInfo gameOverInfo = new GameOverInfo(textpop, eloChange);
                                            gamesRef.child(gameId).child("gameOver").setValue(gameOverInfo);
                                        }
                                    } else {
                                        String blackname = "Joueur noir";
                                        String textpop = blackname + " a gagné !\n"+description;
                                        dialogCallback.show(textpop,eloChange);

                                        GameOverInfo gameOverInfo = new GameOverInfo(textpop, eloChange);
                                        gamesRef.child(gameId).child("gameOver").setValue(gameOverInfo);
                                    }
                                });
                    }

                } else {
                    Log.e("DiffereGameMode", "DialogCallback is null");
                }

                new HistoriquePartieService().ajouterPartie(partie, unused -> {
                    Log.d("Historique", "Partie enregistrée avec succès");
                    gamesRef.child(gameId).removeValue()
                            .addOnSuccessListener(unused2 -> Log.d("Matchmaker", "Partie supprimée"))
                            .addOnFailureListener(e -> Log.e("Matchmaker", "Erreur suppression", e));
                }, e -> Log.e("Historique", "Erreur enregistrement historique", e));
            }
        }).addOnFailureListener(e -> Log.e("GameOver", "Erreur récupération partie", e));

    }

    private String convertMoveToNotation(Move move) {
        if (move.isCastling()) {
            if (move.getToCol() == 6) return "O-O";     // Petit roque
            if (move.getToCol() == 2) return "O-O-O";   // Grand roque
        }

        StringBuilder notation = new StringBuilder();

        String pieceType = move.getPieceType();
        String promotion = move.getPromotion();

        char toFile = (char) ('a' + move.getToCol());
        int toRank = 8 - move.getToRow();
        String toSquare = toFile + String.valueOf(toRank);

        if (!"Pawn".equals(pieceType)) {
            notation.append(getPieceLetter(pieceType));
        }

        if (move.isCapture()) {
            if ("Pawn".equals(pieceType)) {
                char fromFile = (char) ('a' + move.getFromCol());
                notation.append(fromFile);
            }
            notation.append("x");
        }

        notation.append(toSquare);

        if (promotion != null && !promotion.isEmpty()) {
            notation.append("=").append(getPieceLetter(promotion));
        }

        if (move.isCheckmate()) {
            notation.append("#");
        } else if (move.isCheck()) {
            notation.append("+");
        }

        return notation.toString();
    }

    private String getPieceLetter(String pieceType) {
        switch (pieceType) {
            case "Knight": return "N";
            case "Bishop": return "B";
            case "Rook":   return "R";
            case "Queen":  return "Q";
            case "King":   return "K";
            default:       return ""; // Pawn
        }
    }



    @Override
    public void beforeMovePiece(ChessGame game) {
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
    public void setDialogCallback(ChessGameController.GameOverDialogCallback callback) {
        this.dialogCallback = callback;
    }

    public void validateMove(Move move, ChessGame game, ChessBoardView view, PlayerInfoView playerInfoViewWhite, PlayerInfoView playerInfoViewBlack) {
        if (view.isAnimating() || processingMove) {
            return;
        }

        checkIfMyTurn(isMyTurn -> {
            if (!isMyTurn) {
                System.out.println("Ce n'est pas votre tour !");
                return;
            }

            if(!move.getColor().equals(playerColor)){
                System.out.println("Ce n'est pas votre couleur !");
                return;
            }

            boolean moveOk = game.canMovePiece(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol());
            if (moveOk) {
                processingMove = true;
                animateMove(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol(), game.getPiece(move.getFromRow(), move.getFromCol()), view);
                view.setAnimationEndCallback(() -> {
                    System.out.println("AVANT MOVE PIECE");
                    Move finalMove = game.movePiece(move);
                    System.out.println("APRES MOVE PIECE");
                    if(finalMove != null){
                        System.out.println("Coup valide !");



                        if(finalMove.getPromotion()!= null &&finalMove.getPromotion().equals("waiting")){
                            System.out.println("Promotion en attente !");
                        }
                        else {
                            onMoveValidated(finalMove, game, view, playerInfoViewWhite, playerInfoViewBlack);
                            System.out.println("Coup valide 2 !");
                        }



                    }
                    else{
                        System.out.println("Coup invalide !");
                    }
                    System.out.println("Coup valide ou non 3 !");
                    processingMove = false;
                });
            } else {
                System.out.println("Coup invalide !");
            }
        });
    }

    public void animateMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece, ChessBoardView boardView) {
        boardView.animateMove(fromRow, fromCol, toRow, toCol, piece);
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
                System.out.println("Coup reçu : " + snapshot.getValue());
                System.out.println("Nouveau coup reçu : " + snapshot.getValue(Move.class));
                Move move = snapshot.getValue(Move.class);
                if (move != null) {
                    System.out.println("COUP RECU 2");

                    if(!playerColor.equals(move.getColor()) && !view.isAnimating() && !processingMove){
                        System.out.println("COUP RECU 3");

                        processingMove = true;

                        // Récupérer la pièce à déplacer
                        Piece pieceToMove = game.getPiece(move.getFromRow(), move.getFromCol());

                        if (pieceToMove != null) {
                            // Animer le mouvement pour le joueur qui reçoit le coup
                            animateMove(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol(), pieceToMove, view);

                            view.setAnimationEndCallback(() -> {
                                // Après l'animation, appliquer le mouvement sur le plateau
                                Move finalMove = game.movePiece(move);
                                view.invalidate();

                                getCurrentTurn(turn -> {
                                    game.setWhiteTurn(turn.equals("white"));
                                    processingMove = false;
                                });
                            });
                        } else {
                            // Si la pièce est null (ne devrait pas arriver), appliquer directement le coup
                            Move finalMove = game.movePiece(move);
                            view.invalidate();

                            getCurrentTurn(turn -> {
                                game.setWhiteTurn(turn.equals("white"));
                                processingMove = false;
                            });
                        }
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Differe", "Erreur lors de l'écoute des coups", error.toException());
            }
        });
    }

    public void startListeningForGameOver(ChessGame game) {
        gamesRef.child(gameId).child("gameOver").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GameOverInfo gameOverInfo = snapshot.getValue(GameOverInfo.class);
                if (gameOverInfo != null) {

                    String textpop = gameOverInfo.getText();
                    String eloChange = gameOverInfo.getElochange();

                    if (dialogCallback != null) {
                        dialogCallback.show(textpop,eloChange);
                    } else {
                        Log.e("DiffereGameMode", "DialogCallback is null");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Differe", "Erreur écoute gameOver", error.toException());
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