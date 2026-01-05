package com.marwadi.finalprojectchess;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.Toast;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private TextView tvMoveLog;
    private ScrollView moveHistoryScroll;
    private int moveCount = 1;
    private int enPassantTargetRow = -1;
    private int enPassantTargetCol = -1;
    private int halfMoveClock = 0;
    private GridLayout chessBoardGrid;
    private Piece[][] boardState = new Piece[8][8];
    private boolean isWhiteTurn = true;
    private boolean isVsComputerMode = false;

    // Phase 2: Selection Variables
    private int selectedRow = -1;
    private int selectedCol = -1;

    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Add this BEFORE setContentView
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvMoveLog = findViewById(R.id.tvMoveLog);
        moveHistoryScroll = findViewById(R.id.moveHistoryScroll);
        chessBoardGrid = findViewById(R.id.chessBoardGrid);
        Button btnBack = findViewById(R.id.btnBackToMenu);



        soundManager = new SoundManager(this);


        // 1. Get the data sent from the menu
        isVsComputerMode = getIntent().getBooleanExtra("isVsComputer", false);
        Button btnDraw = findViewById(R.id.btnOfferDraw);
        if (isVsComputerMode) {
            btnDraw.setVisibility(View.GONE);
        } else {
            btnDraw.setOnClickListener(v -> showOpponentDrawDialog());
        }

        btnBack.setOnClickListener(v -> showBackDialog());


        // 2. Start the game logic
        setupInitialPieces();
        renderBoard();
    }



    @Override
    protected void onResume() {
        super.onResume();
        // Force a check of the sound setting as soon as the user returns from Settings
        SharedPreferences prefs = getSharedPreferences("ChessPrefs", MODE_PRIVATE);
        boolean soundEnabled = prefs.getBoolean("sound", true);

        // Optional: Log it to your console to see if it's actually changing
        android.util.Log.d("ChessSettings", "Sound is currently: " + soundEnabled);
    }

    private void showBackDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Game")
                .setMessage("Are you sure you want to go back to menu ?")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void checkPawnPromotion(int row, int col) {
        Piece piece = boardState[row][col];
        if (piece != null && piece.type == Piece.Type.PAWN) {
            if ((piece.isWhite && row == 0) || (!piece.isWhite && row == 7)) {
                // If it's the bot's pawn, auto-promote to Queen
                if (isVsComputerMode && !piece.isWhite) {
                    boardState[row][col] = new Piece(Piece.Type.QUEEN, false, R.drawable.queen);
                    renderBoard();
                } else {
                    // Otherwise, show the selection dialog for the human
                    showPromotionDialog(row, col, piece.isWhite);
                }
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
                    // 1. COMPLETELY WIPE the board array first
                    for (int r = 0; r < 8; r++) {
                        for (int c = 0; c < 8; c++) {
                            boardState[r][c] = null;
                        }
                    }

                    // 2. Reset Move History & Counters
                    moveCount = 1;
                    halfMoveClock = 0;
                    enPassantTargetRow = -1;
                    enPassantTargetCol = -1;
                    tvMoveLog.setText("Moves: "); // Clear the visual log

                    // 3. Re-initialize and Redraw
                    setupInitialPieces();
                    isWhiteTurn = true;

                    // 4. Ensure rotation is reset to 0 for the start
                    chessBoardGrid.setRotation(0f);

                    renderBoard();
                })
                .setNegativeButton("Main Menu", (dialog, which) -> finish())
                .show();
    }
    private void executeMove(int row, int col) {
        Piece movingPiece = boardState[selectedRow][selectedCol];
        Piece targetPiece = boardState[row][col];

        // --- ADD THIS LOGIC FOR EN PASSANT TARGETING ---
        // First, reset the target for the current turn
        int oldEnPassantRow = enPassantTargetRow;
        int oldEnPassantCol = enPassantTargetCol;
        enPassantTargetRow = -1;
        enPassantTargetCol = -1;

        // If a pawn moves 2 squares, set the new En Passant target
        if (movingPiece.type == Piece.Type.PAWN && Math.abs(row - selectedRow) == 2) {
            enPassantTargetRow = (selectedRow + row) / 2; // The square the pawn "skipped"
            enPassantTargetCol = col;
        }

        // 1. Move/Capture Sound
        boolean soundEnabled = getSharedPreferences("ChessPrefs", MODE_PRIVATE).getBoolean("sound", true);
        if (soundEnabled) {
            if (targetPiece != null || (movingPiece.type == Piece.Type.PAWN && row == enPassantTargetRow && col == enPassantTargetCol)) {
                soundManager.playCapture();
            } else {
                soundManager.playMove();
            }
        }

        if (movingPiece.type == Piece.Type.PAWN && row == oldEnPassantRow && col == oldEnPassantCol) {
            int capturedPawnRow = isWhiteTurn ? row + 1 : row - 1;
            boardState[capturedPawnRow][col] = null; // Remove the pawn that was jumped over
        }

        // 3. Update Move History Logic
        boolean isCapture = (targetPiece != null) || (movingPiece.type == Piece.Type.PAWN && row == enPassantTargetRow && col == enPassantTargetCol);
        String finalNotation = getMoveNotation(selectedRow, selectedCol, row, col, movingPiece, isCapture);

        if (isWhiteTurn) {
            tvMoveLog.append(moveCount + ". " + finalNotation + " ");
        } else {
            tvMoveLog.append(finalNotation + "  ");
            moveCount++;
        }

        // 4. THE FIX: Clear the EXACT square the piece started from
        boardState[row][col] = movingPiece;
        boardState[selectedRow][selectedCol] = null; // Use selectedRow, not boardState.length - 1
        movingPiece.hasMoved = true;

        // 5. Finalize Turn
        isWhiteTurn = !isWhiteTurn;
        selectedRow = -1;
        selectedCol = -1;

        checkPawnPromotion(row, col);
        renderBoard();
        rotateBoard();

        // 6. Check for End Game or Bot Trigger
        if (!hasLegalMoves(isWhiteTurn)) {
            showGameOverDialog(isInCheck(isWhiteTurn) ? "Checkmate!" : "Stalemate!");
        } else if (isVsComputerMode && !isWhiteTurn) {
            makeComputerMove();
        }
        moveHistoryScroll.post(() -> moveHistoryScroll.fullScroll(View.FOCUS_DOWN));
    }
    private void showOpponentDrawDialog() {
        // Step 1: Opponent sees the offer
        new AlertDialog.Builder(this)
                .setTitle("Draw Offered")
                .setMessage("Your opponent has offered a draw. Do you accept?")
                .setCancelable(false)
                .setPositiveButton("Accept", (dialog, which) -> showConfirmDrawDialog())
                .setNegativeButton("Decline", (dialog, which) -> {
                    Toast.makeText(this, "Draw offer declined. Continue game.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showConfirmDrawDialog() {
        // Step 2: Final confirmation
        new AlertDialog.Builder(this)
                .setTitle("Confirm Draw")
                .setMessage("Are you sure you want to draw?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    showGameOverDialog("Game end as a result of draw");
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Toast.makeText(this, "Returning to board. Continue game.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
    private void makeComputerMove() {
        java.util.List<int[]> legalMoves = new java.util.ArrayList<>();

        // Find all legal moves for Black
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = boardState[r][c];
                if (p != null && !p.isWhite) {
                    for (int tR = 0; tR < 8; tR++) {
                        for (int tC = 0; tC < 8; tC++) {
                            if (isValidMove(r, c, tR, tC) && isMoveSafe(r, c, tR, tC, false)) {
                                legalMoves.add(new int[]{r, c, tR, tC});
                            }
                        }
                    }
                }
            }
        }

        if (!legalMoves.isEmpty()) {
            int[] move = legalMoves.get(new java.util.Random().nextInt(legalMoves.size()));
            final int fR = move[0];
            final int fC = move[1];
            final int tR = move[2];
            final int tC = move[3];

            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (!isFinishing()) {
                    selectedRow = fR;
                    selectedCol = fC;
                    executeMove(tR, tC); // Bypasses the click listener guards!
                }
            }, 1000);
        }
    }

    private void renderBoard() {
        chessBoardGrid.removeAllViews();
        int squareSize = getResources().getDisplayMetrics().widthPixels / 8;

        // Load the "Move Helper" setting from SharedPreferences
        // This now listens to the swHelper switch from your settings
        boolean showHints = getSharedPreferences("ChessPrefs", MODE_PRIVATE)
                .getBoolean("helper", true);

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
                    // 1. Fetch the saved theme from Settings
                    SharedPreferences prefs = getSharedPreferences("ChessPrefs", MODE_PRIVATE);
                    String theme = prefs.getString("board_theme", "brown"); // Default to brown

// 2. Define colors for the chosen theme
                    int lightColor = Color.parseColor("#FFFFFF"); // Light squares are usually white/cream
                    int darkColor;

                    switch (theme) {
                        case "green":
                            darkColor = Color.parseColor("#769656"); // Chess.com Green
                            break;
                        case "blue":
                            darkColor = Color.parseColor("#4B7399"); // Professional Blue
                            break;
                        case "pink":
                            darkColor = Color.parseColor("#E6A8D7"); // Soft Pink
                            break;
                        default: // "brown"
                            darkColor = Color.parseColor("#B58863"); // Classic Wood Brown
                            break;
                    }

// 3. Apply the colors based on the theme
                    if (isKingInCheckSquare) {
                        square.setBackgroundColor(Color.parseColor("#FFCDD2")); // Red highlight for check
                    } else if (row == selectedRow && col == selectedCol) {
                        square.setBackgroundColor(Color.parseColor("#BCED91")); // Selection highlight
                    } else {
                        // This uses the theme colors you just picked
                        square.setBackgroundColor(((row + col) % 2 == 0) ? lightColor : darkColor);
                    }
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
        int tint = piece.isWhite ? R.color.square_light : R.color.piece_black;
        pieceImg.setColorFilter(getResources().getColor(tint));

        FrameLayout.LayoutParams pParams = new FrameLayout.LayoutParams(squareSize - 16, squareSize - 16);
        pParams.gravity = Gravity.CENTER;
        pieceImg.setLayoutParams(pParams);
        square.addView(pieceImg);
    }
    private String getMoveNotation(int fR, int fC, int tR, int tC, Piece actor, boolean isCapture) {
        String[] columns = {"a", "b", "c", "d", "e", "f", "g", "h"};
        String targetSq = columns[tC] + (8 - tR); // e.g., "e4"

        String piecePrefix = "";
        if (actor.type == Piece.Type.KNIGHT) piecePrefix = "N";
        else if (actor.type == Piece.Type.BISHOP) piecePrefix = "B";
        else if (actor.type == Piece.Type.ROOK) piecePrefix = "R";
        else if (actor.type == Piece.Type.QUEEN) piecePrefix = "Q";
        else if (actor.type == Piece.Type.KING) piecePrefix = "K";

        // Pawn captures show the starting column (e.g., "exd5")
        if (actor.type == Piece.Type.PAWN && isCapture) {
            return columns[fC] + "x" + targetSq;
        }

        return piecePrefix + (isCapture ? "x" : "") + targetSq;
    }

    // Phase 2: Handle Click Logic
    private void handleSquareClick(int row, int col) {
        // 1. Guard: If it's the computer's turn, don't let human touch the board
        if (isVsComputerMode && !isWhiteTurn) {
            return;
        }

        if (selectedRow == -1) {
            Piece clicked = boardState[row][col];
            if (clicked != null && clicked.isWhite == isWhiteTurn) {
                // REMOVED: The makeComputerMove() call here was causing the crash
                selectedRow = row;
                selectedCol = col;
                renderBoard();
            }
        } else {
            if (isValidMove(selectedRow, selectedCol, row, col)) {
                if (isMoveSafe(selectedRow, selectedCol, row, col, isWhiteTurn)) {

                    Piece movingPiece = boardState[selectedRow][selectedCol];
                    Piece targetPiece = boardState[row][col];

                    // Sound Logic
                    boolean soundEnabled = getSharedPreferences("ChessPrefs", MODE_PRIVATE).getBoolean("sound", true);
                    if (soundEnabled) {
                        if (targetPiece != null || (movingPiece.type == Piece.Type.PAWN && row == enPassantTargetRow && col == enPassantTargetCol)) {
                            soundManager.playCapture();
                        } else {
                            soundManager.playMove();
                        }
                    }

                    // En Passant Logic
                    if (movingPiece.type == Piece.Type.PAWN && row == enPassantTargetRow && col == enPassantTargetCol) {
                        int capturedPawnRow = isWhiteTurn ? row + 1 : row - 1;
                        boardState[capturedPawnRow][col] = null;
                    }

                    // 50-move rule
                    if (movingPiece.type == Piece.Type.PAWN || targetPiece != null) {
                        halfMoveClock = 0;
                    } else {
                        halfMoveClock++;
                    }

                    // Castling Logic
                    if (movingPiece.type == Piece.Type.KING && Math.abs(col - selectedCol) == 2) {
                        int rookStartCol = (col > selectedCol) ? 7 : 0;
                        int rookEndCol = (col > selectedCol) ? 5 : 3;
                        boardState[row][rookEndCol] = boardState[row][rookStartCol];
                        boardState[row][rookStartCol] = null;
                        if (boardState[row][rookEndCol] != null) boardState[row][rookEndCol].hasMoved = true;
                    }

                    // Finalize Move on Board
                    movingPiece.hasMoved = true;
                    boardState[row][col] = movingPiece;
                    boardState[selectedRow][selectedCol] = null;

                    // --- ADD THE EN PASSANT TARGET SETTING HERE ---
// We must check if this move creates an En Passant opportunity for the NEXT player
                    int nextEnPassantRow = -1;
                    int nextEnPassantCol = -1;

                    if (movingPiece.type == Piece.Type.PAWN && Math.abs(row - selectedRow) == 2) {
                        // Calculate the "skipped" square based on which direction the pawn moved
                        nextEnPassantRow = (selectedRow + row) / 2;
                        nextEnPassantCol = col;
                    }

// Update the global variables so isValidMove() can see them on the next click
                    enPassantTargetRow = nextEnPassantRow;
                    enPassantTargetCol = nextEnPassantCol;

                    // Move History Logging
                    boolean isCapture = (targetPiece != null) || (movingPiece.type == Piece.Type.PAWN && row == enPassantTargetRow && col == enPassantTargetCol);
                    String finalNotation = getMoveNotation(selectedRow, selectedCol, row, col, movingPiece, isCapture);

                    if (isWhiteTurn) {
                        tvMoveLog.append(moveCount + ". " + finalNotation + " ");
                    } else {
                        tvMoveLog.append(finalNotation + "  ");
                        moveCount++;
                    }

                    checkPawnPromotion(row, col);

                    // --- THE TURN SWAP ---
                    isWhiteTurn = !isWhiteTurn;

                    // Update selection state BEFORE checking for computer move
                    selectedRow = -1;
                    selectedCol = -1;

                    renderBoard();
                    rotateBoard();

                    // Check for Checkmate/Stalemate
                    boolean canMove = hasLegalMoves(isWhiteTurn);
                    boolean inCheck = isInCheck(isWhiteTurn);
                    boolean soundEnabledFinal = getSharedPreferences("ChessPrefs", MODE_PRIVATE).getBoolean("sound", true);

                    if (!canMove) {
                        if (inCheck) {
                            if (soundEnabledFinal) soundManager.playCheckmate();
                            showGameOverDialog("Checkmate! " + (isWhiteTurn ? "Black" : "White") + " Wins!");
                        } else {
                            showGameOverDialog("Draw: Stalemate!");
                        }
                    } else {
                        if (inCheck && soundEnabledFinal) soundManager.playCheck();

                        // --- TRIGGER COMPUTER HERE ---
                        // Only if it's currently Black's turn and mode is active
                        if (isVsComputerMode && !isWhiteTurn) {
                            makeComputerMove();
                        }
                    }
                    return; // Exit to avoid the double reset at the bottom
                } else {
                    Toast.makeText(this, "Illegal Move: King in Check!", Toast.LENGTH_SHORT).show();
                }
            }

            // Reset selection for failed moves
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
        private void rotateBoard() {
            // 1. NEW: Check if we are in Computer Mode. If so, stay at 0 degrees.
            if (isVsComputerMode) {
                chessBoardGrid.setRotation(0f);
                for (int i = 0; i < chessBoardGrid.getChildCount(); i++) {
                    chessBoardGrid.getChildAt(i).setRotation(0f);
                }
                return; // Exit early so no rotation happens
            }

            // 2. Read the rotate setting for Multiplayer (Friend mode)
            boolean shouldRotate = getSharedPreferences("ChessPrefs", MODE_PRIVATE)
                    .getBoolean("rotate_enabled", true);

            // 3. If rotation is disabled in settings, force 0 degrees
            if (!shouldRotate) {
                chessBoardGrid.setRotation(0f);
                for (int i = 0; i < chessBoardGrid.getChildCount(); i++) {
                    chessBoardGrid.getChildAt(i).setRotation(0f);
                }
                return;
            }

            // 4. Run the 180-degree animation for Multiplayer mode
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


