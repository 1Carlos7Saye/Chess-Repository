package com.marwadi.finalprojectchess;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
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

    // Track selection for movement logic
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

                // Set Square Color
                int color = ((row + col) % 2 == 0) ? R.color.square_light : R.color.square_dark;
                square.setBackgroundColor(getResources().getColor(color));

                Piece piece = boardState[row][col];
                if (piece != null) {
                    // 1. ADD SHADOW LAYER (10% Opacity Black)
                    ImageView shadow = new ImageView(this);
                    shadow.setImageResource(piece.resId);
                    shadow.setColorFilter(Color.argb(26, 0, 0, 0)); // 26 = ~10% opacity

                    FrameLayout.LayoutParams shadowParams = new FrameLayout.LayoutParams(
                            squareSize - 20, squareSize - 20);
                    shadowParams.gravity = Gravity.CENTER;
                    shadowParams.topMargin = 4; // Offset shadow slightly down
                    shadowParams.leftMargin = 4; // Offset shadow slightly right
                    shadow.setLayoutParams(shadowParams);
                    square.addView(shadow);

                    // 2. ADD MAIN PIECE LAYER
                    ImageView pieceImage = new ImageView(this);
                    pieceImage.setImageResource(piece.resId);
                    int tint = piece.isWhite ? R.color.piece_white_ivory : R.color.piece_black_charcoal;
                    pieceImage.setColorFilter(getResources().getColor(tint));

                    FrameLayout.LayoutParams pieceParams = new FrameLayout.LayoutParams(
                            squareSize - 20, squareSize - 20);
                    pieceParams.gravity = Gravity.CENTER;
                    pieceImage.setLayoutParams(pieceParams);
                    square.addView(pieceImage);
                }

                chessBoardGrid.addView(square);
            }
        }
    }
}