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
        return (ChessPiece) capturedPiece;
    }

    private void validateSourcePosition(Position position){
        if (!board.thereIsAPiece(position))
            throw new ChessException("Não existe peça nessa posição");
        if (!board.piece(position).isThereAnyPossibleMove())
            throw new ChessException("Não existem movimentos possiveis para essa peça!");
    }

    private void validateTargetPosition(Position source, Position target) {
        if (!board.piece(source).possibleMove(target))
            throw new ChessException("Não é possivel mover esta peça para a posição escolhida");
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
