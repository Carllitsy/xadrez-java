package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.King;
import chess.pieces.Rook;

public class ChessMatch {

    private Board board;

    public ChessMatch() {
        board = new Board(8,8);
        initialSetup();
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

    private void placeNewPiece(int row, char column, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(row, column).toPosition());
    }

    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();
        validateSourcePosition(source);
        Piece capturedPiece = makeMove(source, target);
        return (ChessPiece) capturedPiece;
    }

    private void validateSourcePosition(Position position){
        if (!board.thereIsAPiece(position))
            throw new ChessException("Não existe peça nessa posição");
    }

    private Piece makeMove(Position source, Position target) {
        Piece p = board.removePiece(source);
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(p, target);
        return capturedPiece;
    }

    private void initialSetup() {
        placeNewPiece(2, 'c', new Rook(board, Color.WHITE));
        placeNewPiece(1, 'c', new Rook(board, Color.WHITE));
        placeNewPiece(2, 'd', new Rook(board, Color.WHITE));
        placeNewPiece(2, 'e', new Rook(board, Color.WHITE));
        placeNewPiece(1, 'e', new Rook(board, Color.WHITE));
        placeNewPiece(1, 'd', new King(board, Color.WHITE));
        placeNewPiece(7, 'c', new Rook(board, Color.BLACK));
        placeNewPiece(8, 'c', new Rook(board, Color.BLACK));
        placeNewPiece(7, 'd', new Rook(board, Color.BLACK));
        placeNewPiece(7, 'e', new Rook(board, Color.BLACK));
        placeNewPiece(8, 'e', new Rook(board, Color.BLACK));
        placeNewPiece(8, 'd', new King(board, Color.BLACK));
    }
}
