package com.marwadi.finalprojectchess;

public class Piece {
    public enum Type { PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING }

    public Type type;
    public boolean isWhite; // True = Ivory, False = Charcoal
    public int resId;       // This links to your SVG files

    // NEW: Tracks if the piece has moved (required for Castling and En Passant)
    public boolean hasMoved = false;

    public Piece(Type type, boolean isWhite, int resId) {
        this.type = type;
        this.isWhite = isWhite;
        this.resId = resId;
        this.hasMoved = false; // By default, a new piece has not moved
    }
}