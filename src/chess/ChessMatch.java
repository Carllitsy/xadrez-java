package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;

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
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;


    public ChessMatch() {
        board = new Board(8,8);
        turn = 1;
        currentPlayer = Color.WHITE;
        initialSetup();
    }

    public int getTurn() {
        return turn;
    }

    public ChessPiece getEnPassantVulnerable() {
        return enPassantVulnerable;
    }

    public ChessPiece getPromoted() {
        return promoted;
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

        ChessPiece movedPiece = (ChessPiece)board.piece(target);

        promoted = null;
        if (movedPiece instanceof Pawn) {
            if ((movedPiece.getColor() == Color.WHITE && target.getRow() == 0)
                    || (movedPiece.getColor() == Color.BLACK && target.getRow() == 7)) {
                promoted = (ChessPiece)board.piece(target);
                promoted = replacePromotedPiece("R");
            }
        }

        check = testCheck(opponent(currentPlayer));

        if (testCheckMate(opponent(currentPlayer))) {
            checkMate = true;
        }
        else
            nextTurn();

        if (movedPiece instanceof Pawn && (target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2))
            enPassantVulnerable = movedPiece;
        else
            enPassantVulnerable = null;

        return (ChessPiece) capturedPiece;
    }

    public ChessPiece replacePromotedPiece (String type) {
        if (promoted == null)
            throw new IllegalStateException("Não existe peça a ser promovida");
        if (!type.equals("C") && !type.equals("R") && !type.equals("B") && !type.equals("T"))
            return promoted;

        Position pos = promoted.getChessPosition().toPosition();
        Piece p = board.removePiece(pos);
        piecesOnTheBoard.remove(p);

        ChessPiece newPiece = newPiece(type, promoted.getColor());
        board.placePiece(newPiece, pos);
        piecesOnTheBoard.add(newPiece);

        return newPiece;
    }

    private ChessPiece newPiece(String type, Color color) {
        if (type.equals("B")) return new Bishop(board,color);
        if (type.equals("C")) return new Knight(board,color);
        if (type.equals("R")) return new Queen(board,color);
        return new Rook(board, color);
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

        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceR = new Position(source.getRow(), source.getColumn() + 3);
            Position targetR = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece)board.removePiece(sourceR);
            board.placePiece(rook, targetR);
            rook.increaseMoveCount();
        }

        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceR = new Position(source.getRow(), source.getColumn() - 4);
            Position targetR = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece)board.removePiece(sourceR);
            board.placePiece(rook, targetR);
            rook.increaseMoveCount();
        }

        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == null) {
                Position pawnPosition;

                if (p.getColor() == Color.WHITE)
                    pawnPosition = new Position(target.getRow() + 1, target.getColumn());
                else
                    pawnPosition = new Position(target.getRow() - 1, target.getColumn());

                capturedPiece = board.removePiece(pawnPosition);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
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

        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceR = new Position(source.getRow(), source.getColumn() + 3);
            Position targetR = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece)board.removePiece(targetR);
            board.placePiece(rook, sourceR);
            rook.decreaseMoveCount();
        }

        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceR = new Position(source.getRow(), source.getColumn() - 4);
            Position targetR = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece)board.removePiece(targetR);
            board.placePiece(rook, sourceR);
            rook.decreaseMoveCount();
        }

        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == null) {
                Position pawnPosition;
                ChessPiece pawn = (ChessPiece) board.removePiece(target);

                if (p.getColor() == Color.WHITE)
                    pawnPosition = new Position(3, target.getColumn());
                else
                    pawnPosition = new Position(4, target.getColumn());

                board.placePiece(pawn, pawnPosition);
                capturedPiece = board.removePiece(pawnPosition);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
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
        placeNewPiece(1, 'd', new Queen(board, Color.WHITE));
        placeNewPiece(1,'e', new King(board, Color.WHITE, this));
        placeNewPiece(1, 'f', new Bishop(board, Color.WHITE));
        placeNewPiece(1, 'g', new Knight(board, Color.WHITE));
        placeNewPiece(1,'h', new Rook(board, Color.WHITE));
        placeNewPiece(2,'a', new Pawn(board, Color.WHITE, this));
        placeNewPiece(2,'b', new Pawn(board, Color.WHITE, this));
        placeNewPiece(2,'c', new Pawn(board, Color.WHITE, this));
        placeNewPiece(2,'d', new Pawn(board, Color.WHITE, this));
        placeNewPiece(2,'e', new Pawn(board, Color.WHITE, this));
        placeNewPiece(2,'f', new Pawn(board, Color.WHITE, this));
        placeNewPiece(2,'g', new Pawn(board, Color.WHITE, this));
        placeNewPiece(2,'h', new Pawn(board, Color.WHITE, this));

        placeNewPiece( 8,'a', new Rook(board, Color.BLACK));
        placeNewPiece(8,'b', new Knight(board, Color.BLACK));
        placeNewPiece(8, 'c', new Bishop(board, Color.BLACK));
        placeNewPiece(8, 'd', new Queen(board, Color.BLACK));
        placeNewPiece( 8,'e', new King(board, Color.BLACK, this));
        placeNewPiece(8, 'f', new Bishop(board, Color.BLACK));
        placeNewPiece(8,'g', new Knight(board, Color.BLACK));
        placeNewPiece( 8,'h', new Rook(board, Color.BLACK));
        placeNewPiece( 7,'a', new Pawn(board, Color.BLACK, this));
        placeNewPiece( 7,'b', new Pawn(board, Color.BLACK, this));
        placeNewPiece( 7,'c', new Pawn(board, Color.BLACK, this));
        placeNewPiece( 7,'d', new Pawn(board, Color.BLACK, this));
        placeNewPiece( 7,'e', new Pawn(board, Color.BLACK, this));
        placeNewPiece( 7,'f', new Pawn(board, Color.BLACK, this));
        placeNewPiece( 7,'g', new Pawn(board, Color.BLACK, this));
        placeNewPiece( 7,'h', new Pawn(board, Color.BLACK, this));
    }
}
