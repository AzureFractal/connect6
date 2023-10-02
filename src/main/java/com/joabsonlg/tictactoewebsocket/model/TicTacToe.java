package com.joabsonlg.tictactoewebsocket.model;

import com.joabsonlg.tictactoewebsocket.enumeration.GameState;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Class representing a Tic-Tac-Toe game.
 *
 * @author Joabson Arley do Nascimento
 */
public class TicTacToe {
    public static final int BOARD_SIZE = 19;
    private String gameId;
    private String[][] board;
    private String player1;
    private String player2;
    private String winner;
    private String turn;
    private GameState gameState;
    private MeinDisplay meinDisplay;
    private MeinCtrl meinCtrl;
    private int currentSquare = -1;

    public TicTacToe(String player1, String player2) {
        this.gameId = UUID.randomUUID().toString();
        this.player1 = player1;
        this.player2 = player2;
        this.turn = player1;
        this.board = new String[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                this.board[i][j] = "0";
            }
        }
        gameState = GameState.WAITING_FOR_PLAYER;
        this.meinDisplay = new MeinDisplay();
        this.meinCtrl = new MeinCtrl(meinDisplay);
        meinCtrl.resetBoard();
        board = meinCtrl.cur.toStringArray();

        System.out.println(meinCtrl.cur.toString());
        System.out.println(Arrays.deepToString(meinCtrl.cur.toStringArray()));
    }

    /**
     * Makes a move in the specified position on the board.
     *
     * @param player the name of the player making the move
     * @param move   the position of the move
     */
    public void makeMove(String player, int move) {
        currentSquare = meinCtrl.tryMove(currentSquare, move, 0);
        board = meinCtrl.cur.toStringArray();
        // TODO: Make the webpage display our move while the computer is calculating

        // Computer's turn (Code copied from MeinCtrl's calcB)
        if (meinCtrl.curGame.ply % 2 == 0) {
            meinCtrl.resetEvaluationString();
            if (meinCtrl.cur.moveNum / 2 % 2 == 0) {
                meinCtrl.strategy = 0;
                meinCtrl.posVal = meinCtrl.posVal0;
            } else {
                meinCtrl.strategy = 1;
                meinCtrl.posVal = meinCtrl.posVal1;
            }
            int score;
            score = meinCtrl.cur.anaPlay(meinCtrl.stdDepth, meinCtrl.stdQuiet, meinCtrl.OPT_DEFEND);
            meinCtrl.displayEvaluationString(score);
        }
        board = meinCtrl.cur.toStringArray();

//        int row = move / BOARD_SIZE;
//        int col = move % BOARD_SIZE;
//        if (Objects.equals(board[row][col], " ")) {
//            board[row][col] = Objects.equals(player, player1) ? "X" : "O";
//            turn = player.equals(player1) ? player2 : player1;
//            checkWinner();
//            updateGameState();
//        }
    }

    /**
     * Check if there is a winner. If a winning combination is found,
     * the winner is set to the corresponding player.
     */
    private void checkWinner() {
        // TODO: Implement
        return;

//        for (int i = 0; i < BOARD_SIZE; i++) {
//            if (Objects.equals(board[i][0], board[i][1]) && Objects.equals(board[i][0], board[i][2])) {
//                if (!Objects.equals(board[i][0], " ")) {
//                    setWinner(Objects.equals(board[i][0], player1) ? player1 : player2);
//                    return;
//                }
//            }
//        }
    }

    /**
     * Updates the game state based on the current state of the game.
     */
    private void updateGameState() {
        if (winner != null) {
            gameState = winner.equals(player1) ? GameState.PLAYER1_WON : GameState.PLAYER2_WON;
        } else if (isBoardFull()) {
            gameState = GameState.TIE;
        } else {
            gameState = turn.equals(player1) ? GameState.PLAYER1_TURN : GameState.PLAYER2_TURN;
        }
    }

    /**
     * Check if the board is full.
     *
     * @return true if the board is full, false otherwise
     */
    private boolean isBoardFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (Objects.equals(board[i][j], "0")) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if the game is over.
     *
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return winner != null || isBoardFull();
    }

    /**
     * Getters and Setters
     */
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String[][] getBoard() {
        return board;
    }

    public void setBoard(String[][] board) {
        this.board = board;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}

