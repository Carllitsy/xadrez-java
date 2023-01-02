package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessMatch {

    private Board board;
    private int turn;
    private  Color currentPlayer;
    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();
    private boolean check;
    private boolean checkMate;


    public ChessMatch() {
        board = new Board(8,8);
        turn = 1;
        currentPlayer = Color.WHITE;
        initialSetup();
    }

    public int getTurn() {
        return turn;
    }

    public boolean isCheck() {
        return check;
    }

    public boolean isCheckMate() {
        return checkMate;
    }

    private boolean testCheckMate(Color color) {
        if (!testCheck(color))
            return false;

        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
        for (Piece p : list) {
            boolean[][] possible = p.possibleMoves();
            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getColumns(); j++) {
                    if (possible[i][j]) {
                        Position source = ((ChessPiece)p).getChessPosition().toPosition();
                        Position target = new Position(i,j);
                        Piece capturedPiece = makeMove(source, target);
                        boolean testCheck = testCheck(color);
                        undoMove(source, target, capturedPiece);
                        if (!testCheck)
                            return false;
                    }
                }
            }
        }
        return true;
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public ChessPiece[][] getPieces() {
        ChessPiece[][] aux = new ChessPiece[board.getRows()][board.getColumns()];
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                aux[i][j] = (ChessPiece) board.piece(i, j);
            }
        }
        return aux;
    }

    private Color opponent(Color color) {
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private ChessPiece king(Color color) {
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
        for (Piece p : list)
            if (p instanceof King)
                return (ChessPiece) p;

        throw new IllegalStateException("Não existe rei desta cor");
    }

    private boolean testCheck(Color color) {
        Position kingPosition = king(color).getChessPosition().toPosition();
        List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());
        for (Piece p : opponentPieces) {
            boolean[][] possible = p.possibleMoves();
            if (possible[kingPosition.getRow()][kingPosition.getColumn()])
                return true;
        }
        return false;
    }

    private void placeNewPiece(int row, char column, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(row, column).toPosition());
        piecesOnTheBoard.add(piece);
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition) {
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return board.piece(position).possibleMoves();
    }

    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();
        validateSourcePosition(source);
        validateTargetPosition(source, target);
        Piece capturedPiece = makeMove(source, target);

        if (testCheck(currentPlayer)) {
            undoMove(source, target, capturedPiece);
            throw new ChessException("Você não pode se colocar em check!");
        }

        check = testCheck(opponent(currentPlayer));

        if (testCheckMate(opponent(currentPlayer))) {
            checkMate = true;
        }
        else
            nextTurn();

        return (ChessPiece) capturedPiece;
    }

    private void validateSourcePosition(Position position){
        if (!board.thereIsAPiece(position))
            throw new ChessException("Não existe peça nessa posição");
        if (currentPlayer != ((ChessPiece)board.piece(position)).getColor())
            throw new ChessException("Esta peça pertence ao adversário");
        if (!board.piece(position).isThereAnyPossibleMove())
            throw new ChessException("Não existem movimentos possiveis para esta peça!");
    }

    private void validateTargetPosition(Position source, Position target) {
        if (!board.piece(source).possibleMove(target))
            throw new ChessException("Não é possivel mover esta peça para a posição escolhida");
    }

    private Piece makeMove(Position source, Position target) {
        ChessPiece p = (ChessPiece) board.removePiece(source);
        p.increaseMoveCount();
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(p, target);

        if (capturedPiece != null) {
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }

        return capturedPiece;
    }

    private void undoMove(Position source, Position target, Piece capturedPiece) {
        ChessPiece p = (ChessPiece) board.removePiece(target);
        p.decreaseMoveCount();
        board.placePiece(p, source);

        if (capturedPiece != null){
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }
    }

    private void nextTurn() {
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private void initialSetup() {
        placeNewPiece(1,'a', new Rook(board, Color.WHITE));
        placeNewPiece(1,'b', new Knight(board, Color.WHITE));
        placeNewPiece(1, 'c', new Bishop(board, Color.WHITE));
        placeNewPiece(1,'e', new King(board, Color.WHITE));
        placeNewPiece(1, 'f', new Bishop(board, Color.WHITE));
        placeNewPiece(1, 'g', new Knight(board, Color.WHITE));
        placeNewPiece(1,'h', new Rook(board, Color.WHITE));
        placeNewPiece(2,'a', new Pawn(board, Color.WHITE));
        placeNewPiece(2,'b', new Pawn(board, Color.WHITE));
        placeNewPiece(2,'c', new Pawn(board, Color.WHITE));
        placeNewPiece(2,'d', new Pawn(board, Color.WHITE));
        placeNewPiece(2,'e', new Pawn(board, Color.WHITE));
        placeNewPiece(2,'f', new Pawn(board, Color.WHITE));
        placeNewPiece(2,'g', new Pawn(board, Color.WHITE));
        placeNewPiece(2,'h', new Pawn(board, Color.WHITE));

        placeNewPiece( 8,'a', new Rook(board, Color.BLACK));
        placeNewPiece(8,'b', new Knight(board, Color.BLACK));
        placeNewPiece(8, 'c', new Bishop(board, Color.BLACK));
        placeNewPiece( 8,'e', new King(board, Color.BLACK));
        placeNewPiece(8, 'f', new Bishop(board, Color.BLACK));
        placeNewPiece(8,'g', new Knight(board, Color.BLACK));
        placeNewPiece( 8,'h', new Rook(board, Color.BLACK));
        placeNewPiece( 7,'a', new Pawn(board, Color.BLACK));
        placeNewPiece( 7,'b', new Pawn(board, Color.BLACK));
        placeNewPiece( 7,'c', new Pawn(board, Color.BLACK));
        placeNewPiece( 7,'d', new Pawn(board, Color.BLACK));
        placeNewPiece( 7,'e', new Pawn(board, Color.BLACK));
        placeNewPiece( 7,'f', new Pawn(board, Color.BLACK));
        placeNewPiece( 7,'g', new Pawn(board, Color.BLACK));
        placeNewPiece( 7,'h', new Pawn(board, Color.BLACK));
    }
}
