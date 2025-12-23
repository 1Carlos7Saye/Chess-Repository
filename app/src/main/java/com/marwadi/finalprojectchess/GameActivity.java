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


    private int enPassantTargetRow = -1;
    private int enPassantTargetCol = -1;
    private int halfMoveClock = 0;
    private GridLayout chessBoardGrid;
    private Piece[][] boardState = new Piece[8][8];
    private boolean isWhiteTurn = true;

    // Phase 2: Selection Variables
    private int selectedRow = -1;
    private int selectedCol = -1;

    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        soundManager = new SoundManager(this);

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

    private void checkPawnPromotion(int row, int col) {
        Piece piece = boardState[row][col];
        if (piece != null && piece.type == Piece.Type.PAWN) {
            if ((piece.isWhite && row == 0) || (!piece.isWhite && row == 7)) {
                showPromotionDialog(row, col, piece.isWhite);
            }
        }
    }

    private void showPromotionDialog(int row, int col, boolean isWhite) {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        new AlertDialog.Builder(this)
                .setTitle("Pawn Promotion")
                .setItems(options, (dialog, which) -> {
                    Piece.Type newType;
                    int newResId;
                    switch (which) {
                        case 1:
                            newType = Piece.Type.ROOK;
                            newResId = R.drawable.rook;
                            break;
                        case 2:
                            newType = Piece.Type.BISHOP;
                            newResId = R.drawable.bishop;
                            break;
                        case 3:
                            newType = Piece.Type.KNIGHT;
                            newResId = R.drawable.knight;
                            break;
                        default:
                            newType = Piece.Type.QUEEN;
                            newResId = R.drawable.queen;
                            break;
                    }
                    boardState[row][col] = new Piece(newType, isWhite, newResId);
                    renderBoard();
                })
                .setCancelable(false)
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
        for (int i = 0; i < 8; i++)
            boardState[1][i] = new Piece(Piece.Type.PAWN, false, R.drawable.pawn);

        // White Pieces
        for (int i = 0; i < 8; i++)
            boardState[6][i] = new Piece(Piece.Type.PAWN, true, R.drawable.pawn);
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

        // Load the "Move Helper" setting from SharedPreferences
        boolean showHints = getSharedPreferences("ChessPrefs", MODE_PRIVATE)
                .getBoolean("helper_enabled", true);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                FrameLayout square = new FrameLayout(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(row), GridLayout.spec(col));
                params.width = squareSize;
                params.height = squareSize;
                square.setLayoutParams(params);

                Piece piece = boardState[row][col];

                // 1. Check for King in Check (Red highlight)
                boolean isKingInCheckSquare = false;
                if (piece != null && piece.type == Piece.Type.KING) {
                    if (isInCheck(piece.isWhite)) {
                        isKingInCheckSquare = true;
                    }
                }

                // 2. Set Background Priority
                if (isKingInCheckSquare) {
                    square.setBackgroundColor(Color.parseColor("#FFCDD2"));
                } else if (row == selectedRow && col == selectedCol) {
                    square.setBackgroundColor(Color.parseColor("#BCED91"));
                } else {
                    int color = ((row + col) % 2 == 0) ? R.color.square_light : R.color.square_dark;
                    square.setBackgroundColor(getResources().getColor(color));
                }

                // 3. NEW: Add Legal Move Hints (Translucent Dots)
                if (selectedRow != -1 && showHints) {
                    // Only show a dot if the move is physically possible AND doesn't put King in check
                    if (isValidMove(selectedRow, selectedCol, row, col) &&
                            isMoveSafe(selectedRow, selectedCol, row, col, isWhiteTurn)) {

                        ImageView hintDot = new ImageView(this);
                        hintDot.setImageResource(R.drawable.hint_dot_shape);

                        hintDot.setClickable(false);
                        hintDot.setFocusable(false);

                        // Center the dot and make it about 1/3 the square size
                        FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(
                                squareSize / 3, squareSize / 3);
                        dotParams.gravity = Gravity.CENTER;
                        hintDot.setLayoutParams(dotParams);

                        square.addView(hintDot);
                    }
                }

                // 4. Setup interaction & Draw pieces
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

                    Piece movingPiece = boardState[selectedRow][selectedCol];
                    Piece targetPiece = boardState[row][col];

                    // --- NEW: SOUND LOGIC FOR MOVES AND CAPTURES ---
                    // We check for targetPiece OR an active En Passant capture square
                    if (targetPiece != null || (movingPiece.type == Piece.Type.PAWN && row == enPassantTargetRow && col == enPassantTargetCol)) {
                        soundManager.playCapture();
                    } else {
                        soundManager.playMove();
                    }

                    // 1. En Passant Capture Execution
                    if (movingPiece.type == Piece.Type.PAWN && row == enPassantTargetRow && col == enPassantTargetCol) {
                        int capturedPawnRow = isWhiteTurn ? row + 1 : row - 1;
                        boardState[capturedPawnRow][col] = null;
                    }

                    // 2. 50-move rule
                    if (movingPiece.type == Piece.Type.PAWN || targetPiece != null) {
                        halfMoveClock = 0;
                    } else {
                        halfMoveClock++;
                    }

                    // 3. Castling Execution
                    if (movingPiece.type == Piece.Type.KING && Math.abs(col - selectedCol) == 2) {
                        int rookStartCol = (col > selectedCol) ? 7 : 0;
                        int rookEndCol = (col > selectedCol) ? 5 : 3;
                        boardState[row][rookEndCol] = boardState[row][rookStartCol];
                        boardState[row][rookStartCol] = null;
                        if (boardState[row][rookEndCol] != null) boardState[row][rookEndCol].hasMoved = true;
                    }

                    // 4. Update En Passant Target for next turn
                    int nextEnPassantRow = -1;
                    int nextEnPassantCol = -1;
                    if (movingPiece.type == Piece.Type.PAWN && Math.abs(row - selectedRow) == 2) {
                        nextEnPassantRow = (selectedRow + row) / 2;
                        nextEnPassantCol = col;
                    }
                    enPassantTargetRow = nextEnPassantRow;
                    enPassantTargetCol = nextEnPassantCol;

                    // 5. Finalize Move
                    movingPiece.hasMoved = true;
                    boardState[row][col] = movingPiece;
                    boardState[selectedRow][selectedCol] = null;

                    checkPawnPromotion(row, col);
                    isWhiteTurn = !isWhiteTurn;
                    rotateBoard();

                    // --- NEW: SOUND LOGIC FOR CHECK AND CHECKMATE ---
                    boolean canMove = hasLegalMoves(isWhiteTurn);
                    boolean inCheck = isInCheck(isWhiteTurn);

                    if (!canMove) {
                        if (inCheck) {
                            soundManager.playCheckmate(); // Play checkmate sound
                            showGameOverDialog("Checkmate! " + (isWhiteTurn ? "Black" : "White") + " Wins!");
                        } else {
                            showGameOverDialog("Draw: Stalemate!");
                        }
                    } else if (inCheck) {
                        soundManager.playCheck(); // Play standard check alert
                    } else if (halfMoveClock >= 100) {
                        showGameOverDialog("Draw: 50-Move Rule!");
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

    private boolean isPathClear(int fR, int fC, int tR, int tC) {
        int stepR = Integer.compare(tR, fR);
        int stepC = Integer.compare(tC, fC);

        int currR = fR + stepR;
        int currC = fC + stepC;

        // Loop until we reach the target square
        while (currR != tR || currC != tC) {
            // If we hit any piece on the way, the path is blocked
            if (boardState[currR][currC] != null) return false;

            currR += stepR;
            currC += stepC;
        }
        return true;
    }


    // Phase 2: Basic Move Rules
    private boolean isValidMove(int fR, int fC, int tR, int tC) {
        if (fR == tR && fC == tC) return false;

        Piece actor = boardState[fR][fC];
        Piece target = boardState[tR][tC];

        if (target != null && target.isWhite == isWhiteTurn) return false;

        int dR = tR - fR;
        int absDR = Math.abs(dR);
        int dC = tC - fC;
        int absDC = Math.abs(dC);

        if (actor.type == Piece.Type.PAWN) {
            int direction = isWhiteTurn ? -1 : 1;
            int startRow = isWhiteTurn ? 6 : 1;

            // 1-Square Forward
            if (absDC == 0 && dR == direction && target == null) return true;

            // 2-Square Jump
            if (absDC == 0 && fR == startRow && dR == 2 * direction && target == null) {
                Piece pathBlock = boardState[fR + direction][fC];
                return pathBlock == null;
            }

            // Capture & En Passant
            if (absDC == 1 && dR == direction) {
                if (target != null && target.isWhite != isWhiteTurn) return true;
                if (target == null && tR == enPassantTargetRow && tC == enPassantTargetCol) return true;
            }
            return false;
        }

        if (actor.type == Piece.Type.KNIGHT) return (absDR == 2 && absDC == 1) || (absDR == 1 && absDC == 2);
        if (actor.type == Piece.Type.ROOK) return (fR == tR || fC == tC) && isPathClear(fR, fC, tR, tC);
        if (actor.type == Piece.Type.BISHOP) return (absDR == absDC) && isPathClear(fR, fC, tR, tC);
        if (actor.type == Piece.Type.QUEEN) return ((fR == tR || fC == tC) || (absDR == absDC)) && isPathClear(fR, fC, tR, tC);

        if (actor.type == Piece.Type.KING) {
            if (absDR <= 1 && absDC <= 1) return true;
            if (absDR == 0 && absDC == 2 && !actor.hasMoved && !isInCheck(isWhiteTurn)) {
                int rookCol = (tC > fC) ? 7 : 0;
                Piece rook = boardState[fR][rookCol];
                if (rook != null && rook.type == Piece.Type.ROOK && !rook.hasMoved) {
                    if (isPathClear(fR, fC, fR, rookCol)) {
                        int middleCol = (tC > fC) ? fC + 1 : fC - 1;
                        return isMoveSafe(fR, fC, fR, middleCol, isWhiteTurn);
                    }
                }
            }
        }
        return false;
    }



        // Phase 2: Board Flip Animation
        private void rotateBoard () {
            float angle = isWhiteTurn ? 0f : 180f;
            chessBoardGrid.animate().rotation(angle).setDuration(600).start();
            for (int i = 0; i < chessBoardGrid.getChildCount(); i++) {
                chessBoardGrid.getChildAt(i).animate().rotation(angle).setDuration(600).start();
            }
        }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundManager != null) {
            soundManager.release(); //
        }
    }
    }


