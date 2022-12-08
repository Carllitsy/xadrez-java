package chess.pieces;

import boardgame.Board;
import boardgame.Position;
import chess.ChessPiece;
import chess.Color;

public class King extends ChessPiece {

    public King(Board board, Color color) {
        super(board, color);
    }

    @Override
    public String toString() {
        return "K";
    }

    @Override
    public boolean[][] possibleMoves() {
        boolean[][] mat = new boolean[getBoard().getRows()][getBoard().getColumns()];
        Position p = new Position(0,0);

        p.setValues(position.getRow() - 1, position.getColumn() -1);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (getBoard().positionExists(p) && (!getBoard().thereIsAPiece(p) || isThereOpponentPiece(p)))
                    mat[p.getRow()][p.getColumn()] = true;
                p.setColumn(p.getColumn() + 1);
            }
            p.setColumn(position.getColumn() - 1);
            p.setRow(p.getRow() + 1);
        }
        return mat;
    }
}
