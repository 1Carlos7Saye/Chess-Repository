package com.marwadi.finalprojectchess;

import android.widget.Toast;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private GridLayout chessBoardGrid;
    private Piece[][] boardState = new Piece[8][8];
    private boolean isWhiteTurn = true;

    // Phase 2: Selection Variables
    private int selectedRow = -1;
    private int selectedCol = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        chessBoardGrid = findViewById(R.id.chessBoardGrid);
        Button btnBack = findViewById(R.id.btnBackToMenu);

        btnBack.setOnClickListener(v -> showBackDialog());

        setupInitialPieces();
        renderBoard();
    }

    private void showBackDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Game")
                .setMessage("Are you sure you want to go back to menus?")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setupInitialPieces() {
        // Black Pieces
        boardState[0][0] = new Piece(Piece.Type.ROOK, false, R.drawable.rook);
        boardState[0][1] = new Piece(Piece.Type.KNIGHT, false, R.drawable.knight);
        boardState[0][2] = new Piece(Piece.Type.BISHOP, false, R.drawable.bishop);
        boardState[0][3] = new Piece(Piece.Type.QUEEN, false, R.drawable.queen);
        boardState[0][4] = new Piece(Piece.Type.KING, false, R.drawable.king);
        boardState[0][5] = new Piece(Piece.Type.BISHOP, false, R.drawable.bishop);
        boardState[0][6] = new Piece(Piece.Type.KNIGHT, false, R.drawable.knight);
        boardState[0][7] = new Piece(Piece.Type.ROOK, false, R.drawable.rook);
        for (int i = 0; i < 8; i++) boardState[1][i] = new Piece(Piece.Type.PAWN, false, R.drawable.pawn);

        // White Pieces
        for (int i = 0; i < 8; i++) boardState[6][i] = new Piece(Piece.Type.PAWN, true, R.drawable.pawn);
        boardState[7][0] = new Piece(Piece.Type.ROOK, true, R.drawable.rook);
        boardState[7][1] = new Piece(Piece.Type.KNIGHT, true, R.drawable.knight);
        boardState[7][2] = new Piece(Piece.Type.BISHOP, true, R.drawable.bishop);
        boardState[7][3] = new Piece(Piece.Type.QUEEN, true, R.drawable.queen);
        boardState[7][4] = new Piece(Piece.Type.KING, true, R.drawable.king);
        boardState[7][5] = new Piece(Piece.Type.BISHOP, true, R.drawable.bishop);
        boardState[7][6] = new Piece(Piece.Type.KNIGHT, true, R.drawable.knight);
        boardState[7][7] = new Piece(Piece.Type.ROOK, true, R.drawable.rook);
    }
    private boolean hasLegalMoves(boolean isWhite) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardState[r][c];
                if (p != null && p.isWhite == isWhite) {
                    // Test every possible destination square
                    for (int targetR = 0; targetR < 8; targetR++) {
                        for (int targetC = 0; targetC < 8; targetC++) {
                            if (isValidMove(r, c, targetR, targetC)) {
                                if (isMoveSafe(r, c, targetR, targetC, isWhite)) {
                                    return true; // Found at least one legal move!
                                }
                            }
                        }
                    }
                }
            }
        }
        return false; // No legal moves found
    }
    private void showGameOverDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Play Again", (dialog, which) -> {
                    setupInitialPieces();
                    isWhiteTurn = true;
                    renderBoard();
                })
                .setNegativeButton("Main Menu", (dialog, which) -> finish())
                .show();
    }
    private void renderBoard() {
        chessBoardGrid.removeAllViews();
        int squareSize = getResources().getDisplayMetrics().widthPixels / 8;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                FrameLayout square = new FrameLayout(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(row), GridLayout.spec(col));
                params.width = squareSize;
                params.height = squareSize;
                square.setLayoutParams(params);

                Piece piece = boardState[row][col];

                // 1. Identify if this specific square is a King in danger
                boolean isKingInCheckSquare = false;
                if (piece != null && piece.type == Piece.Type.KING) {
                    if (isInCheck(piece.isWhite)) {
                        isKingInCheckSquare = true;
                    }
                }

                // 2. Set Background Priority: Check (Red) -> Selection (Green) -> Default (Board)
                if (isKingInCheckSquare) {
                    // Soft Red color to indicate the King must move or be protected
                    square.setBackgroundColor(Color.parseColor("#FFCDD2"));
                } else if (row == selectedRow && col == selectedCol) {
                    square.setBackgroundColor(Color.parseColor("#BCED91")); // Light Green
                } else {
                    int color = ((row + col) % 2 == 0) ? R.color.square_light : R.color.square_dark;
                    square.setBackgroundColor(getResources().getColor(color));
                }

                // 3. Setup interaction
                final int r = row;
                final int c = col;
                square.setOnClickListener(v -> handleSquareClick(r, c));

                if (piece != null) {
                    addPieceToSquare(square, piece, squareSize);
                }
                chessBoardGrid.addView(square);
            }
        }
    }

    private void addPieceToSquare(FrameLayout square, Piece piece, int squareSize) {
        // 1. Corrected Shadow Layer (10% Opacity)
        ImageView shadow = new ImageView(this);
        shadow.setImageResource(piece.resId);
        shadow.setColorFilter(Color.argb(26, 0, 0, 0)); // Subtle black shadow

        // We use a smaller padding (16 instead of 24) so pieces are larger
        FrameLayout.LayoutParams sParams = new FrameLayout.LayoutParams(squareSize - 16, squareSize - 16);
        sParams.gravity = Gravity.CENTER;
        sParams.topMargin = 6;  // Vertical offset for depth
        sParams.leftMargin = 6; // Horizontal offset for depth
        shadow.setLayoutParams(sParams);
        square.addView(shadow);

        // 2. Main Piece Layer
        ImageView pieceImg = new ImageView(this);
        pieceImg.setImageResource(piece.resId);

        // Applying the Ivory/Charcoal tints from your colors.xml
        int tint = piece.isWhite ? R.color.piece_white_ivory : R.color.piece_black_charcoal;
        pieceImg.setColorFilter(getResources().getColor(tint));

        FrameLayout.LayoutParams pParams = new FrameLayout.LayoutParams(squareSize - 16, squareSize - 16);
        pParams.gravity = Gravity.CENTER;
        pieceImg.setLayoutParams(pParams);
        square.addView(pieceImg);
    }

    // Phase 2: Handle Click Logic
    private void handleSquareClick(int row, int col) {
        if (selectedRow == -1) {
            Piece clicked = boardState[row][col];
            if (clicked != null && clicked.isWhite == isWhiteTurn) {
                selectedRow = row;
                selectedCol = col;
                renderBoard();
            }
        } else {
            if (isValidMove(selectedRow, selectedCol, row, col)) {
                if (isMoveSafe(selectedRow, selectedCol, row, col, isWhiteTurn)) {

                    boardState[row][col] = boardState[selectedRow][selectedCol];
                    boardState[selectedRow][selectedCol] = null;

                    isWhiteTurn = !isWhiteTurn;
                    rotateBoard();
                    renderBoard();

                    // CHECK FOR GAME OVER
                    boolean canMove = hasLegalMoves(isWhiteTurn);
                    boolean inCheck = isInCheck(isWhiteTurn);

                    if (!canMove) {
                        if (inCheck) {
                            // Checkmate: The player who just moved wins
                            String winner = (!isWhiteTurn) ? "White Wins!" : "Black Wins!";
                            showGameOverDialog("Checkmate! " + winner);
                        } else {
                            // Stalemate: No moves, but not in check
                            showGameOverDialog("Draw: Stalemate!");
                        }
                    }
                } else {
                    Toast.makeText(this, "Illegal Move: King would be in Check!", Toast.LENGTH_SHORT).show();
                }
            }
            selectedRow = -1;
            selectedCol = -1;
            renderBoard();
        }
    }

    private boolean isMoveSafe(int fR, int fC, int tR, int tC, boolean whiteKing) {
        // 1. Save the current state to undo later
        Piece movingPiece = boardState[fR][fC];
        Piece targetPiece = boardState[tR][tC];

        // 2. Simulate the move
        boardState[tR][tC] = movingPiece;
        boardState[fR][fC] = null;

        // 3. Check if the King is in danger after this move
        boolean inCheck = isInCheck(whiteKing);

        // 4. Undo the move (Essential to keep the real board correct)
        boardState[fR][fC] = movingPiece;
        boardState[tR][tC] = targetPiece;

        // If not in check, the move is safe!
        return !inCheck;
    }

    // Helper method to show the alert
    private void showCheckDialog() {
        String player = isWhiteTurn ? "White" : "Black";
        new AlertDialog.Builder(this)
                .setTitle("Check!")
                .setMessage("The " + player + " King is in danger.")
                .setPositiveButton("OK", null)
                .show();
    }
    private int[] findKing(boolean whiteKing) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardState[r][c];
                if (p != null && p.type == Piece.Type.KING && p.isWhite == whiteKing) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    private boolean isInCheck(boolean whiteKing) {
        int[] kingPos = findKing(whiteKing);
        if (kingPos == null) return false;

        // Check every square to see if an enemy piece can reach the King
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardState[r][c];
                if (p != null && p.isWhite != whiteKing) {
                    // Temporarily switch turn to check if this enemy piece has a valid move
                    boolean originalTurn = isWhiteTurn;
                    isWhiteTurn = !whiteKing;
                    if (isValidMove(r, c, kingPos[0], kingPos[1])) {
                        isWhiteTurn = originalTurn;
                        return true;
                    }
                    isWhiteTurn = originalTurn;
                }
            }
        }
        return false;
    }

    // Phase 2: Basic Move Rules
    private boolean isValidMove(int fR, int fC, int tR, int tC) {
        if (fR == tR && fC == tC) return false;

        Piece actor = boardState[fR][fC];
        Piece target = boardState[tR][tC]; // Fixed: changed boardState[tR][tR] to [tR][tC]

        // Global Rule: Cannot capture your own color
        if (target != null && target.isWhite == isWhiteTurn) return false;

        int dR = tR - fR;
        int absDR = Math.abs(dR);
        int dC = tC - fC;
        int absDC = Math.abs(dC);

        // 1. PAWN LOGIC
        if (actor.type == Piece.Type.PAWN) {
            int direction = isWhiteTurn ? -1 : 1;
            if (absDC == 0 && dR == direction && target == null) return true;
            int startRow = isWhiteTurn ? 6 : 1;
            if (absDC == 0 && fR == startRow && dR == 2 * direction) {
                Piece pathBlock = boardState[fR + direction][fC];
                return target == null && pathBlock == null;
            }
            return (absDC == 1 && dR == direction && target != null && target.isWhite != isWhiteTurn);
        }

        // 2. KNIGHT LOGIC
        if (actor.type == Piece.Type.KNIGHT) {
            return (absDR == 2 && absDC == 1) || (absDR == 1 && absDC == 2);
        }

        // 3. ROOK LOGIC (Straight lines + Path check)
        if (actor.type == Piece.Type.ROOK) {
            if (fR == tR || fC == tC) return isPathClear(fR, fC, tR, tC);
            return false;
        }

        // 4. BISHOP LOGIC (Diagonals + Path check)
        if (actor.type == Piece.Type.BISHOP) {
            if (absDR == absDC) return isPathClear(fR, fC, tR, tC);
            return false;
        }

        // 5. QUEEN LOGIC (Rook OR Bishop rules)
        if (actor.type == Piece.Type.QUEEN) {
            if ((fR == tR || fC == tC) || (absDR == absDC)) return isPathClear(fR, fC, tR, tC);
            return false;
        }

        // 6. KING LOGIC (One square in any direction)
        if (actor.type == Piece.Type.KING) {
            return absDR <= 1 && absDC <= 1;
        }

        return false;
    }

    private boolean isPathClear(int fR, int fC, int tR, int tC) {
        int stepR = Integer.compare(tR, fR);
        int stepC = Integer.compare(tC, fC);
        int currR = fR + stepR;
        int currC = fC + stepC;

        while (currR != tR || currC != tC) {
            if (boardState[currR][currC] != null) return false; // Path is blocked!
            currR += stepR;
            currC += stepC;
        }
        return true;
    }

    // Phase 2: Board Flip Animation
    private void rotateBoard() {
        float angle = isWhiteTurn ? 0f : 180f;
        chessBoardGrid.animate().rotation(angle).setDuration(600).start();
        for (int i = 0; i < chessBoardGrid.getChildCount(); i++) {
            chessBoardGrid.getChildAt(i).animate().rotation(angle).setDuration(600).start();
        }
    }
}
