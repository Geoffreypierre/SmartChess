package com.example.smartchess.chess.chessboard;



import com.example.smartchess.chess.chessboard.pieces.Bishop;
import com.example.smartchess.chess.chessboard.pieces.King;
import com.example.smartchess.chess.chessboard.pieces.Knight;
import com.example.smartchess.chess.chessboard.pieces.Pawn;
import com.example.smartchess.chess.chessboard.pieces.Piece;
import com.example.smartchess.chess.chessboard.pieces.Queen;
import com.example.smartchess.chess.chessboard.pieces.Rook;

import java.util.List;

public class ChessGame {
    public static final int BOARD_SIZE = 8;
    private Piece[][] board;
    private boolean whiteTurn;

    private OnPieceCapturedListener pieceCapturedListener;

    public void setOnPieceCapturedListener(OnPieceCapturedListener listener) {
        this.pieceCapturedListener = listener;
    }


    private Move enPassantSquare = null;

    public Move getEnPassantSquare() {
        return enPassantSquare;
    }

    public void setEnPassantSquare(Move move) {
     
        enPassantSquare = move;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public ChessGame() {
        board = new Piece[BOARD_SIZE][BOARD_SIZE];
        initBoard();
        whiteTurn = true; // Les blancs commencent
    }

    public void initBoard() {
        // Placement des pions
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new Pawn(Piece.Color.BLACK);
            board[6][i] = new Pawn(Piece.Color.WHITE);
        }
        // Tours
        board[0][0] = new Rook(Piece.Color.BLACK);
        board[0][7] = new Rook(Piece.Color.BLACK);
        board[7][0] = new Rook(Piece.Color.WHITE);
        board[7][7] = new Rook(Piece.Color.WHITE);
        // Cavaliers
        board[0][1] = new Knight(Piece.Color.BLACK);
        board[0][6] = new Knight(Piece.Color.BLACK);
        board[7][1] = new Knight(Piece.Color.WHITE);
        board[7][6] = new Knight(Piece.Color.WHITE);
        // Fous
        board[0][2] = new Bishop(Piece.Color.BLACK);
        board[0][5] = new Bishop(Piece.Color.BLACK);
        board[7][2] = new Bishop(Piece.Color.WHITE);
        board[7][5] = new Bishop(Piece.Color.WHITE);
        // Reines
        board[0][3] = new Queen(Piece.Color.BLACK);
        board[7][3] = new Queen(Piece.Color.WHITE);
        // Rois
        board[0][4] = new King(Piece.Color.BLACK);
        board[7][4] = new King(Piece.Color.WHITE);
    }

    public Piece[][] getBoard() {
        return board;
    }

    public Piece getPiece(int row, int col) {
        if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
            return board[row][col];
        }
        return null;
    }

    public boolean movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow < 0 || fromRow >= BOARD_SIZE || fromCol < 0 || fromCol >= BOARD_SIZE ||
                toRow < 0 || toRow >= BOARD_SIZE || toCol < 0 || toCol >= BOARD_SIZE) {
            return false;
        }
        Piece piece = board[fromRow][fromCol];
        if (piece == null) return false;
        if ((piece.getColor() == Piece.Color.WHITE && !whiteTurn) ||
                (piece.getColor() == Piece.Color.BLACK && whiteTurn)) {
            return false;
        }
        Piece target = board[toRow][toCol];
        // Interdire de capturer ses propres pièces
        if (target != null && target.getColor() == piece.getColor()) {
            return false;
        }


        List<Move> moves = piece.getAvailableMovesWithCheck(fromRow, fromCol, board, enPassantSquare);
        Move proposedMove = new Move(toRow, toCol);
        if (!moves.contains(proposedMove)) {
            return false;
        }



        //Si c'est une capture d'abord on appelle takePiece avant de déplacer mon pion sur la case
        if (target != null) {
            takePiece(toRow, toCol, whiteTurn);
        }
        // Déplacer la pièce
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        piece.setMoved(true);


        // --- Gestion du roque pour le roi ---
        if (piece instanceof King) {
            int colDiff = toCol - fromCol;
            if (Math.abs(colDiff) == 2) { // le roi se déplace de 2 cases, donc c'est un roque
                if (colDiff > 0) {
                    // Petit roque : la tour part de la colonne 7 et se place en colonne 5
                    Piece rook = board[toRow][7];
                    board[toRow][5] = rook;
                    board[toRow][7] = null;
                    if (rook != null) {
                        rook.setMoved(true);
                    }
                } else {
                    // Grand roque : la tour part de la colonne 0 et se place en colonne 3
                    Piece rook = board[toRow][0];
                    board[toRow][3] = rook;
                    board[toRow][0] = null;
                    if (rook != null) {
                        rook.setMoved(true);
                    }
                }
            }
        }


        // Gérer la prise en passant et la promotion

        if (piece instanceof Pawn) {
            Pawn pawn = (Pawn) piece;

            // Si le pion se déplace sur une case d'en passant, supprimer le pion adverse
            if (enPassantSquare != null && toRow == enPassantSquare.getRow() && toCol == enPassantSquare.getCol()) {
                System.out.println("En passant");
                if (whiteTurn) {
                    takePiece(toRow + 1, toCol, whiteTurn); // Supprimer le pion noir

                } else {
                    takePiece(toRow - 1, toCol, whiteTurn); // Supprimer le pion blanc
                }
            }

            setEnPassantSquare(null); // Réinitialiser la case d'en passant

            if (Math.abs(fromRow - toRow) == 2 && Math.abs(fromCol - toCol) == 0) { //déplacement de deux cases donc un enpassantsquare est créé
                // En passant
                System.out.println("En passant");
                if (whiteTurn) { //CREATION D'UNE CASE EN PASSANT
                    setEnPassantSquare(new Move(fromRow - 1, fromCol));
                } else {
                    setEnPassantSquare(new Move(fromRow + 1, fromCol));
                }

            } else {
                System.out.println("Pas en passant");
                setEnPassantSquare(null);
            }


            if (pawn.isPromotionRow(toRow, board)) {

                //CHOIX DE LA PIECE DE PROMOTION

                System.out.println("Promotion du pion en dame");

                Piece promotedPiece = pawn.promote("queen");
                board[toRow][toCol] = promotedPiece;
            }
        }
        else {
            // Réinitialiser la case d'en passant car une piece a été joué et ce n'est pas un pion
            setEnPassantSquare(null);
        }

        whiteTurn = !whiteTurn; // Changer le tour

        System.out.println("Piece moved !");



        //verifier si l'adversaire peut encore bouger des pieces avec la fonction CanAPieceMove de ChessUtils
        System.out.println("VERIF FIN DE PARTIE");
        System.out.println(piece.getColor());
        if(!ChessUtils.CanAPieceMove(board, piece.getColor() == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE, enPassantSquare)){

            System.out.println("PAT OU CHECKMATE");
            if (ChessUtils.isKingInCheck(board, piece.getColor() == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE, enPassantSquare)) {
                System.out.println("CHECKMATE");
            } else {
                System.out.println("PAT");
            }
        }

        return true;
    }


    // Prise d'une pièce avec indication de qui capture
    public void takePiece(int row, int col, boolean capturedByWhite) {
        if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
            Piece piece = board[row][col];
            if (piece != null) {
                System.out.println("Piece taken: " + piece.getClass().getSimpleName() + " at (" + row + ", " + col + ")");
                board[row][col] = null;
                if (pieceCapturedListener != null) {
                    pieceCapturedListener.onPieceCaptured(piece, capturedByWhite);
                }
            }
        }
    }


    public interface OnPieceCapturedListener {
        /**
         * Notifie qu'une pièce a été capturée.
         * @param capturedPiece La pièce qui a été capturée.
         * @param capturedByWhite true si la capture a été effectuée par les blancs, false sinon.
         */
        void onPieceCaptured(Piece capturedPiece, boolean capturedByWhite);
    }




}
