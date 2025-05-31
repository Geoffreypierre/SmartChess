package com.example.smartchess.chess.chessboard;

import com.example.smartchess.chess.chessboard.pieces.Bishop;
import com.example.smartchess.chess.chessboard.pieces.King;
import com.example.smartchess.chess.chessboard.pieces.Knight;
import com.example.smartchess.chess.chessboard.pieces.Pawn;
import com.example.smartchess.chess.chessboard.pieces.Piece;
import com.example.smartchess.chess.chessboard.pieces.Queen;
import com.example.smartchess.chess.chessboard.pieces.Rook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChessGame {
    public static final int BOARD_SIZE = 8;
    private Piece[][] board;
    private boolean whiteTurn;

    public interface OnPromotionCompletedListener {
        void onPromotionCompleted(Move move);
    }

    private OnPromotionCompletedListener promotionCompletedListener;

    public void setOnPromotionCompletedListener(OnPromotionCompletedListener listener) {
        this.promotionCompletedListener = listener;
    }

    public enum BoardOrientation {
        WHITE,
        BLACK
    }

    private BoardOrientation boardOrientation = BoardOrientation.WHITE;

    public void setBoardOrientation(BoardOrientation orientation) {
        this.boardOrientation = orientation;
    }

    public BoardOrientation getBoardOrientation() {
        return boardOrientation;
    }

    private OnPieceCapturedListener pieceCapturedListener;
    private OnPromotionNeededListener promotionNeededListener;

    public interface GameOverCallback {
        void onGameOver(String winner, String loser, String description);
    }

    public interface OnPromotionNeededListener {
        void onPromotionNeeded(int row, int col, Piece.Color pieceColor, PromotionCallback callback);
    }

    public interface PromotionCallback {
        void onPromotionSelected(String promotionType);
    }

    private GameOverCallback gameOverCallback;

    public void setGameOverCallback(GameOverCallback callback) {
        this.gameOverCallback = callback;
    }

    public void setOnPieceCapturedListener(OnPieceCapturedListener listener) {
        this.pieceCapturedListener = listener;
    }

    public void setOnPromotionNeededListener(OnPromotionNeededListener listener) {
        this.promotionNeededListener = listener;
    }

    public void setWhiteTurn(boolean whiteTurn) {
        this.whiteTurn = whiteTurn;
    }

    private Position enPassantSquare = null;

    public Position getEnPassantSquare() {
        return enPassantSquare;
    }

    public void setEnPassantSquare(Position move) {
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

    public boolean canMovePiece(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow < 0 || fromRow >= BOARD_SIZE || fromCol < 0 || fromCol >= BOARD_SIZE ||
                toRow < 0 || toRow >= BOARD_SIZE || toCol < 0 || toCol >= BOARD_SIZE) {
            System.out.println("Invalid move: out of bounds");
            return false;
        }
        Piece piece = board[fromRow][fromCol];
        if (piece == null) return false;
        if ((piece.getColor() == Piece.Color.WHITE && !whiteTurn) ||
                (piece.getColor() == Piece.Color.BLACK && whiteTurn)) {
            System.out.println("Invalid move: not your turn");
            return false;
        }
        Piece target = board[toRow][toCol];
        if (target != null && target.getColor() == piece.getColor()) {
            System.out.println("Invalid move: cannot capture your own piece");
            return false;
        }

        List<Position> moves = piece.getAvailableMovesWithCheck(fromRow, fromCol, board, enPassantSquare);
        Position proposedMove = new Position(toRow, toCol);
        if (!moves.contains(proposedMove)) {
            System.out.println("Invalid move: not a valid move for this piece");
            return false;
        }

        return true;
    }

    public Move movePiece(Move move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();
        String color = move.getColor();
        String pieceType = move.getPieceType();

        Move finalMove = new Move(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol(), move.getColor(), move.getPieceType());

        if (fromRow < 0 || fromRow >= BOARD_SIZE || fromCol < 0 || fromCol >= BOARD_SIZE ||
                toRow < 0 || toRow >= BOARD_SIZE || toCol < 0 || toCol >= BOARD_SIZE) {
            System.out.println("Invalid move: out of bounds");
            return null;
        }

        Piece piece = board[fromRow][fromCol];
        if (piece == null) return null;
        if ((piece.getColor() == Piece.Color.WHITE && !whiteTurn) ||
                (piece.getColor() == Piece.Color.BLACK && whiteTurn)) {
            System.out.println("Invalid move: not your turn");
            return null;
        }

        Piece target = board[toRow][toCol];
        if (target != null && target.getColor() == piece.getColor()) {
            System.out.println("Invalid move: cannot capture your own piece");
            return null;
        }

        List<Position> moves = piece.getAvailableMovesWithCheck(fromRow, fromCol, board, enPassantSquare);
        Position proposedMove = new Position(toRow, toCol);
        if (!moves.contains(proposedMove)) {
            System.out.println("Invalid move: not a valid move for this piece");
            return null;
        }

        if (target != null) {
            takePiece(toRow, toCol, whiteTurn);
            finalMove.setCapture(true);
        }

        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        piece.setMoved(true);

        if (piece instanceof King) {
            int colDiff = toCol - fromCol;
            if (Math.abs(colDiff) == 2) {
                if (colDiff > 0) {
                    Piece rook = board[toRow][7];
                    board[toRow][5] = rook;
                    board[toRow][7] = null;
                    if (rook != null) {
                        rook.setMoved(true);
                    }
                    finalMove.setCastling(true);
                } else {
                    Piece rook = board[toRow][0];
                    board[toRow][3] = rook;
                    board[toRow][0] = null;
                    if (rook != null) {
                        rook.setMoved(true);
                    }
                    finalMove.setCastling(true);
                }
            }
        }

        if (piece instanceof Pawn) {
            Pawn pawn = (Pawn) piece;

            if (enPassantSquare != null && toRow == enPassantSquare.getRow() && toCol == enPassantSquare.getCol()) {
                System.out.println("En passant");
                if (whiteTurn) {
                    takePiece(toRow + 1, toCol, whiteTurn);
                } else {
                    takePiece(toRow - 1, toCol, whiteTurn);
                }
            }

            setEnPassantSquare(null);

            if (Math.abs(fromRow - toRow) == 2 && Math.abs(fromCol - toCol) == 0) {
                System.out.println("En passant");
                if (whiteTurn) {
                    setEnPassantSquare(new Position(fromRow - 1, fromCol));
                } else {
                    setEnPassantSquare(new Position(fromRow + 1, fromCol));
                }
            } else {
                System.out.println("Pas en passant");
                setEnPassantSquare(null);
            }

            if (pawn.isPromotionRow(toRow, board)) {
                System.out.println("Promotion du pion nécessaire");

                final Move moveFinal = finalMove;
                final int promotionRow = toRow;
                final int promotionCol = toCol;

                //cas multi dans lequel on reçoit un coup deja promu
                if (move.getPromotion() != null) {
                    System.out.println("Promotion déjà effectuée : " + move.getPromotion());
                    Piece promotedPiece = pawn.promote(move.getPromotion());
                    board[promotionRow][promotionCol] = promotedPiece;
                    finalMove.setPromotion(move.getPromotion());

                } else {
                    finalMove.setPromotion("waiting");
                    System.out.println("Pas de promotion déjà effectuée");

                    if (promotionNeededListener != null) {
                        promotionNeededListener.onPromotionNeeded(toRow, toCol, piece.getColor(),
                                new PromotionCallback() {
                                    @Override
                                    public void onPromotionSelected(String promotionType) {
                                        Piece promotedPiece = pawn.promote(promotionType);
                                        board[promotionRow][promotionCol] = promotedPiece;
                                        moveFinal.setPromotion(promotionType);
                                        finalMove.setPromotion(promotionType);

                                        System.out.println("Promotion effectuée : " + promotionType);

                                        checkGameState(promotedPiece, moveFinal);

                                        if (promotionCompletedListener != null) {
                                            promotionCompletedListener.onPromotionCompleted(moveFinal);
                                        }

                                    }

                                });
                        System.out.println("before return final move promote");



                    } else {
                        System.out.println("Pas de listener pour la promotion, promotion automatique en dame");
                        Piece promotedPiece = pawn.promote("queen");
                        board[toRow][toCol] = promotedPiece;
                        finalMove.setPromotion("queen");
                    }

                }




            }
        } else {
            setEnPassantSquare(null);
        }

        System.out.println("Piece moved!");

        if (!(piece instanceof Pawn && ((Pawn) piece).isPromotionRow(toRow, board) && promotionNeededListener != null)) {
            checkGameState(piece, finalMove);
        }

        System.out.println("Before return final move last");
        return finalMove;
    }

    private void checkGameState(Piece piece, Move finalMove) {
        Piece.Color opponentColor = piece.getColor() == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE;

        if (!ChessUtils.CanAPieceMove(board, opponentColor, enPassantSquare)) {
            if (ChessUtils.isKingInCheck(board, opponentColor, enPassantSquare)) {
                System.out.println("CHECKMATE");
                finalMove.setCheckmate(true);
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver(
                            piece.getColor() == Piece.Color.WHITE ? "White" : "Black",
                            opponentColor == Piece.Color.WHITE ? "White" : "Black",
                            "échec et mat"
                    );
                }
            } else {
                System.out.println("PAT");
                if (gameOverCallback != null) {
                    gameOverCallback.onGameOver(null, null, "Partie nulle par pat");
                }
            }
        }
    }

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

    public List<List<Map<String, String>>> serializeBoard() {
        List<List<Map<String, String>>> boardState = new ArrayList<>();

        for (int row = 0; row < BOARD_SIZE; row++) {
            List<Map<String, String>> rowList = new ArrayList<>();
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = board[row][col];
                Map<String, String> cell = new HashMap<>();
                if (piece != null) {
                    cell.put("type", piece.getClass().getSimpleName());
                    cell.put("color", piece.getColor().toString());
                } else {
                    cell.put("type", "EMPTY");
                    cell.put("color", "NONE");
                }
                rowList.add(cell);
            }
            boardState.add(rowList);
        }

        return boardState;
    }

    public void loadBoardFromState(List<List<Map<String, String>>> savedState) {
        board = new Piece[BOARD_SIZE][BOARD_SIZE];

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Map<String, String> cell = savedState.get(row).get(col);
                String type = cell.get("type");
                String colorStr = cell.get("color");

                if (type == null || "EMPTY".equals(type)) {
                    board[row][col] = null;
                    continue;
                }

                Piece.Color color = Piece.Color.valueOf(colorStr);

                switch (type) {
                    case "Pawn":
                        board[row][col] = new Pawn(color);
                        break;
                    case "Rook":
                        board[row][col] = new Rook(color);
                        break;
                    case "Knight":
                        board[row][col] = new Knight(color);
                        break;
                    case "Bishop":
                        board[row][col] = new Bishop(color);
                        break;
                    case "Queen":
                        board[row][col] = new Queen(color);
                        break;
                    case "King":
                        board[row][col] = new King(color);
                        break;
                    default:
                        board[row][col] = null;
                        break;
                }
            }
        }
    }

    public Map<String, Object> serializeFullState() {
        Map<String, Object> state = new HashMap<>();

        // Plateau de jeu
        List<List<Map<String, String>>> boardState = serializeBoard();
        state.put("board", boardState);

        // En passant
        if (enPassantSquare != null) {
            Map<String, Integer> enPassant = new HashMap<>();
            enPassant.put("row", enPassantSquare.getRow());
            enPassant.put("col", enPassantSquare.getCol());
            state.put("enPassant", enPassant);
        } else {
            state.put("enPassant", null);
        }

        return state;
    }

    public void loadFullState(Map<String, Object> savedState) {
        // Charger le plateau
        List<List<Map<String, String>>> boardState = (List<List<Map<String, String>>>) savedState.get("board");
        loadBoardFromState(boardState);

        // Charger en passant
        Object enPassantObj = savedState.get("enPassant");
        if (enPassantObj instanceof Map) {
            Map<String, Long> enPassantMap = (Map<String, Long>) enPassantObj;
            int row = enPassantMap.get("row").intValue();
            int col = enPassantMap.get("col").intValue();
            enPassantSquare = new Position(row, col);
        } else {
            enPassantSquare = null;
        }

    }




}