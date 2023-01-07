package chess.pieces;

import boardgame.Board;
import boardgame.Position;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.Color;

public class Pawn extends ChessPiece {

    private ChessMatch chessMatch;

    public Pawn(Board board, Color color, ChessMatch chessMatch) {
        super(board, color);
        this.chessMatch = chessMatch;
    }

    @Override
    public boolean[][] possibleMoves() {
        boolean[][] mat = new boolean[getBoard().getRows()][getBoard().getColumns()];
        int color = getColor() == Color.WHITE ? -1 : 1;
        Position p = new Position(0,0);
        Position p2 = new Position(position.getRow() + color, position.getColumn());

        p.setValues(position.getRow() + color, position.getColumn());
        if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p))
            mat[p.getRow()][p.getColumn()] = true;

        p.setValues(position.getRow() + 2 * color, position.getColumn());
        if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)
                && getBoard().positionExists(p2) && !getBoard().thereIsAPiece(p2) && getMoveCount() == 0)
            mat[p.getRow()][p.getColumn()] = true;

        p.setValues(position.getRow() + color, position.getColumn() - 1);
        if (getBoard().positionExists(p) && isThereOpponentPiece(p))
            mat[p.getRow()][p.getColumn()] = true;

        p.setValues(position.getRow() + color, position.getColumn() + 1);
        if (getBoard().positionExists(p) && isThereOpponentPiece(p))
            mat[p.getRow()][p.getColumn()] = true;

        if (position.getRow() == 3 && this.getColor() == Color.WHITE) {
            Position left = new Position(position.getRow(), position.getColumn() - 1);
            Position right = new Position(position.getRow(), position.getColumn() + 1);

            if (getBoard().positionExists(left) && isThereOpponentPiece(left)
                    && getBoard().piece(left) == chessMatch.getEnPassantVulnerable()) {
                mat[left.getRow() - 1][left.getColumn()] = true;
            }

            if (getBoard().positionExists(right) && isThereOpponentPiece(right)
                    && getBoard().piece(right) == chessMatch.getEnPassantVulnerable()) {
                mat[right.getRow() - 1][right.getColumn()] = true;
            }
        }

        if (position.getRow() == 4 && this.getColor() == Color.BLACK) {
            Position left = new Position(position.getRow(), position.getColumn() - 1);
            Position right = new Position(position.getRow(), position.getColumn() + 1);

            if (getBoard().positionExists(left) && isThereOpponentPiece(left)
                    && getBoard().piece(left) == chessMatch.getEnPassantVulnerable()) {
                mat[left.getRow() + 1][left.getColumn()] = true;
            }

            if (getBoard().positionExists(right) && isThereOpponentPiece(right)
                    && getBoard().piece(right) == chessMatch.getEnPassantVulnerable()) {
                mat[right.getRow() + 1][right.getColumn()] = true;
            }
        }

        return mat;
        }

    @Override
    public String toString() {
        return "P";
    }
}
