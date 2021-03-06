import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import javax.swing.border.Border;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * an Applet for an object oriented MemoryGame
 * new
 *
 * @author: Lam Ngo, James Murphy, Varun Shah and Diego Bazan
 * @version: 03/09/2017
 */
public class MainGame extends JApplet {
    private static final long serialVersionUID = 1L;
    private static final String turn = "Turns: ";
    private static final String you = "Matches: ";
    private static final String computer = "Computer: ";
    private final int APPLET_WIDTH = 850, APPLET_HEIGHT = 700;

    private MemoryGame myGame; //the memory game object that is going to play the game
    private static CanvasPanel canvasPanel;
    private Container cp;
    private JPanel memoryPanel, menuPanel,picPanel;  //the panels of the game
    private CustomButton flipButton,removeButton, mainmenuButton, newGameButton, computerMode,
            soloMode, quitButton,logOut,music,mute;; // the various button
    private JLabel turnCounter,timeCounter, yourMatch, computerMatch, gameMode, welcomeMes; // print text on screen
    private boolean remove; // when to remove
    private int turnCount; // count the number of turn
    private Timer timer; // clock countdown
    private int time;
    private ComputerAI hal; // AI
    private boolean playComputer; // when it is in computer mode
    private String userName; // userID
    private FileWrite writeMe; // write to a file
    private FileRead readMe; // Read from a file
    private boolean taken = false; // if name is already taken
    private String nextToken; // next item in file
    private FileWrite writePersonalHighScores; // Write to highscore file
    private FileRead readPer; // read highscore file
    private String usersFile = "Users.txt";
    private AudioInputStream audio1; //maintheme
    private AudioInputStream audio2; // cardflip
    private Clip mainTheme;
    private Clip cardFlip;
    private JLabel picLabel;
    private BufferedImage myPicture; // group pic
    /**
     * Set up the buttons and canvas and register the listeners.
     */
    public void init(){
        try {
            // Set cross-platform Java L&F
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }

        myPicture = null;
        try {
            myPicture = ImageIO.read(MainGame.class.getResourceAsStream("images/groupPic.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Welcome Panel with group picture.
        welcomeMes = new JLabel();
        picLabel = new JLabel(new ImageIcon(myPicture));
        picPanel = new JPanel();
        picPanel.setLayout(new BorderLayout());
        picPanel.setBackground(Color.magenta);
        welcomeMes.setFont(new Font("Monospaced",Font.PLAIN,30));
        picLabel.setHorizontalAlignment(SwingConstants.CENTER);
        picPanel.add(welcomeMes, BorderLayout.CENTER);
        picPanel.add(picLabel, BorderLayout.SOUTH);

        // Get the theme song and card flip sound
        try{
            audio1 = AudioSystem.getAudioInputStream(this.getClass().getResource("Memories_song_clip.wav"));
            mainTheme = AudioSystem.getClip();
            mainTheme.open(audio1);

            audio2 = AudioSystem.getAudioInputStream(this.getClass().getResource("cardflip.wav"));
            cardFlip = AudioSystem.getClip();
            cardFlip.open(audio2);
        }
        catch(Exception ex)
        {
        }

        //choose between modes panel
        menuPanel = new JPanel();
        menuPanel.setLayout(new FlowLayout());
        computerMode =new CustomButton("play with computer");
        soloMode = new CustomButton("Play solo");
        quitButton = new CustomButton("Quit");
        logOut = new CustomButton("LogOut");
        music = new CustomButton("music");
        mute = new CustomButton("mute");
        computerMode.addActionListener(new ComputerButtonListener());
        soloMode.addActionListener(new SoloModeButtonListener());
        quitButton.addActionListener(new QuitButtonListener());
        logOut.addActionListener(new LogOutButtonListener());
        music.addActionListener(new MusicButtonListener());
        mute.addActionListener((new MuteButtonListener()));
        menuPanel.add(soloMode);
        menuPanel.add(computerMode);
        menuPanel.add(music);
        menuPanel.add(mute);
        menuPanel.add(logOut);
        menuPanel.add(quitButton);
        menuPanel.setBackground(Color.magenta);

        //Memory Panel to play Memory
        memoryPanel = new JPanel();
        newGameButton = new CustomButton("new game");
        mainmenuButton = new CustomButton("main menu");
        removeButton = new CustomButton("remove");
        flipButton = new CustomButton("flip");
        turnCounter = new JLabel(turn + turnCount);
        yourMatch = new JLabel(you + 0);
        computerMatch = new JLabel();
        gameMode = new JLabel();
        removeButton.setBackground(Color.white);
        flipButton.setBackground(Color.white);
        mainmenuButton.setBackground(Color.white);
        newGameButton.setBackground(Color.white);
        memoryPanel.setBackground(Color.cyan);
        memoryPanel.add(gameMode);
        memoryPanel.add(newGameButton);
        memoryPanel.add(removeButton);
        memoryPanel.add(flipButton);
        memoryPanel.add(mainmenuButton);
        memoryPanel.add(turnCounter);
        memoryPanel.add(yourMatch);
        memoryPanel.add(computerMatch);
        memoryPanel.setLayout(new BoxLayout(memoryPanel,BoxLayout.PAGE_AXIS));

        newGameButton.addActionListener(new NewGameButtonListener());
        mainmenuButton.addActionListener(new MainMenuButtonListener());
        flipButton.addActionListener(new FlipButtonListener());
        removeButton.addActionListener(new RemoveButtonListener());

        canvasPanel = new CanvasPanel();
        canvasPanel.setBackground(Color.pink);

        myGame = new MemoryGame(); // initialize computer AI
        playComputer = false;

        turnCount = 0;
        timeCounter = new JLabel();
        memoryPanel.add(timeCounter);
        time = 0;
        timer = new Timer(1000, new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                time++;
                if (time < 60){
                    if (time < 10){
                        timeCounter.setText("0" + ":" + "0" + time);
                    }else{
                        timeCounter.setText("0" + ":" + time);
                    }
                }else{
                    int min = time/60;
                    if (min < 10 && (time - min*60 < 10)){
                        timeCounter.setText(0 + min + ":" + 0 + (time - min*60));
                    }else{
                        timeCounter.setText(min + ":" + (time - min*60));
                    }
                }

            }
        });
        timer.start(); // timer to count time upward

        //used to create/edit the file containing the list of users
        writeMe = new FileWrite(usersFile, true);
        readMe = new FileRead(usersFile);
        logIn();
        welcomeMes.setText("            Welcome to our game: " + userName);
        mainTheme.start();
        mainTheme.loop(Clip.LOOP_CONTINUOUSLY);

        cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(menuPanel, BorderLayout.NORTH);
        cp.add(picPanel, BorderLayout.CENTER);

        cp.setBackground(Color.magenta);
        setSize(APPLET_WIDTH, APPLET_HEIGHT);


    }

    /**
     * Update name when a new user logs in.
     */
    private void updateName(){
        welcomeMes.setText("            Welcome to our game: " + userName);
    }

    /**
     * Handle getting user name. Cover all the possiblities.
     */
    private void logIn(){
        readMe = new FileRead(usersFile);
        userName = JOptionPane.showInputDialog("Enter your 3 letter username.");
        if (userName == null){
            System.exit(0);
        }else{
            userName = userName.toUpperCase();
        }
        while (userName == "" || userName.length() != 3){
            userName = JOptionPane.showInputDialog("The user name must be 3 characters, please try again").toUpperCase();
        }
        while ((readMe.hasNextToken() == true)) {
            nextToken = readMe.nextToken();
            if (nextToken.equals(userName)){
                String[] options = { "yes", "use this id" };
                int result = JOptionPane.showOptionDialog(null, "This username already exists, do you want to make your own?", "Warning",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                if (result == 0){
                    taken = false;
                    userName = JOptionPane.showInputDialog("make a new one");
                    readMe = new FileRead(usersFile);
                    if (userName == null){
                        System.exit(0);
                    }else{
                        userName = userName.toUpperCase();
                    }
                    while (userName == "" || userName.length() != 3){
                        userName = JOptionPane.showInputDialog("The user name must be 3 characters, please try again");
                        if (userName == null){
                            System.exit(0);
                        }else{
                            userName = userName.toUpperCase();
                        }
                    }
                }else{
                    taken = true;
                }
            }
        }
        try{
            if (!taken){
                writeMe.write(userName);
            }
        } catch (IOException ioe){

        }
        String personalHighScores = userName + ".txt";
        writePersonalHighScores = new FileWrite(personalHighScores,true);
        readPer = new FileRead(personalHighScores);
    }

    /**
     * Update turns and amtch count
     */
    private void turnUpdate(){
        turnCounter.setText(turn + turnCount);
        yourMatch.setText(you + myGame.getMatchCounter());
        if (playComputer){
            computerMatch.setText(computer + hal.getMatchCount());
        }
        memoryPanel.revalidate();
    }

    /**
     * What to do when play computer button is pressed
     */
    private class ComputerButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            mainTheme.stop();
            turnCount = 0;
            String[] options = { "easy", "medium","insane"};
            int result = JOptionPane.showOptionDialog((Component) null,
                    "which mode do you want to play?",
                    "Computer Mode",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[2]);
            if (result != -1){
                time = 0;
                computerMatch.setVisible(true);
                computerMatch.setText(computer + 0);
                cp.removeAll();
                cp.add(memoryPanel,BorderLayout.EAST);
                cp.add(canvasPanel,BorderLayout.CENTER);
                cp.revalidate();
                myGame.reDeal();
                myGame.deSelectCards();
                hal = new ComputerAI(myGame.getUserChoices(),myGame.getDeck(),result);
                playComputer  = true;
                if (result == 0){
                    gameMode.setText("easy mode");
                }else if (result == 1){
                    gameMode.setText("medium mode");
                }else if (result == 2){
                    gameMode.setText("INSANE");
                }
            }
            turnUpdate();
            repaint();
        }
    }

    /**
     * What to do when Solo Mode Button is pressed is pressed.
     */
    private class SoloModeButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            mainTheme.stop();
            myGame.reDeal();
            time = 0;
            turnCount = 0;
            cp.removeAll();
            gameMode.setText("Solo Mode");
            playComputer = false;
            computerMatch.setVisible(false);
            turnUpdate();
            cp.add(memoryPanel,BorderLayout.EAST);
            cp.add(canvasPanel,BorderLayout.CENTER);
            cp.revalidate();
            repaint();
        }
    }

    /**
     * What to do when Music is pressed.
     */
    private class MusicButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            mainTheme.start();
        }
    }

    /**
     * What to do when Mute Button is pressed.
     */
    private class MuteButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            mainTheme.stop();
        }
    }

    /**
     * What to do when newGameButton is pressed.
     */
    private class NewGameButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String[] options = { "yes", "no" };
            int result = JOptionPane.showOptionDialog(null, "Are you sure?", "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            if (result == 0) {
                myGame.reDeal();
                turnCount = 0;
                remove = false;
                time = 0;
                turnUpdate();
            }
            repaint();
        }
    }

    /**
     * Check when the game ends and print high score if in solo mode
     */
    private boolean checkEnd() {
        if (myGame.checkEndCondition()) {
            String[] options = {"yes", "to main menu"};
            String message;
            if (playComputer){
                if (myGame.getMatchCounter() < hal.getMatchCount()){
                    message = "hal beats you. wanna play again?";
                }else if (myGame.getMatchCounter() > hal.getMatchCount()){
                    message = "you BEAT HAL. wanna play again?";
                }else{
                    message = "TIE GAME. wanna play again?";
                }
            }else{
                try{
                    writePersonalHighScores.write(turnCount + "");
                    writePersonalHighScores.sortHighScore();
                } catch (IOException ioe){

                }
                message = "you beat solo mode. wanna play again?";
                String highscore= "Your score: " + turnCount + "\n" + "Your High score: ";
                while (readPer.hasNextToken()){
                    highscore += "\n" + readPer.nextToken();
                }
                JOptionPane.showMessageDialog(null, highscore);
            }

            int result = JOptionPane.showOptionDialog(null, message, "u the best",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);

            if (result == 0) {
                myGame.deSelectCards();
                myGame.reDeal();
                time = 0;
                turnCount = 0;
                if (playComputer){
                    hal.newGame();
                }
                turnUpdate();
            } else {
                mainTheme.start();
                cp.removeAll();
                cp.add(menuPanel,BorderLayout.NORTH);
                cp.add(picPanel, BorderLayout.CENTER);
                cp.revalidate();
            }
            repaint();
            return true;
        }else{
            repaint();
            return false;
        }
    }

    /**
     * What to do when Remove is pressed.
     */
    private class RemoveButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (myGame.removeandflipable()){
                if (remove){
                    JOptionPane.showMessageDialog(null, "that's a match!");
                    myGame.removeSelected();
                    remove = true;
                    cardFlip.start();
                    cardFlip.setFramePosition(0);
                    turnUpdate();
                    repaint();
                    checkEnd();
                }else{
                    remove = false;
                    JOptionPane.showMessageDialog(null, "Not a match, please flip!");
                }
            }else{
                JOptionPane.showMessageDialog(null, "please choose two cards first.");
            }
            repaint();
        }
    }

    /**
     * What to do when Main menu Button is pressed.
     */
    private class MainMenuButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String[] options = { "yes", "no" };
            int result = JOptionPane.showOptionDialog(null, "Are you sure?", "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            if (result == 0){
                myGame.deSelectCards();
                mainTheme.start();
                cp.removeAll();
                cp.add(menuPanel,BorderLayout.NORTH);
                cp.add(picPanel, BorderLayout.CENTER);
                cp.revalidate();
            }
            repaint();
        }
    }

    /**
     * What to do when Quit Button is pressed.
     */
    private class QuitButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent event){
            String[] options = { "yes", "no" };
            int result = JOptionPane.showOptionDialog(null, "Are you sure?", "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            if (result == 0){
                System.exit(0);
            }
        }
    }


    /**
     * What to do when log out button is pressed.
     */
    private class LogOutButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent event){
            String[] options = { "yes", "no" };
            int result = JOptionPane.showOptionDialog(null, "Are you sure?", "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            if (result == 0){
                String[] options1 = { "Log in", "quit" };
                result = JOptionPane.showOptionDialog(null, "" +
                                "You are logged out. Wanna log in again or quit?", "Warning",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options1, options1[0]);
                if (result == 0){
                    logIn();
                    mainTheme.start();
                }else{
                    System.exit(0);
                }
            }
            updateName();
        }
    }

    /**
     * What to do when Flip is pressed.
     */
    private class FlipButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if(myGame.removeandflipable()){
                if (remove){
                    JOptionPane.showMessageDialog(null, "they are a match, don't flip");
                }else if (playComputer){
                    cardFlip.start();
                    cardFlip.setFramePosition(0);
                    myGame.deSelectCards();
                    repaint();
                    JOptionPane.showMessageDialog(null, "Computer Turn");
                    hal.getCards();
                    repaint();
                    while (hal.computerTurn()){
                        turnUpdate();
                        if (checkEnd()){
                            break;
                        }
                        cardFlip.start();
                        cardFlip.setFramePosition(0);
                        myGame.deSelectCards();
                        hal.getCards();
                        repaint();
                    }
                    myGame.deSelectCards();
                    cardFlip.start();
                    cardFlip.setFramePosition(0);
                }else{
                    cardFlip.start();
                    cardFlip.setFramePosition(0);
                    myGame.deSelectCards();
                }
            }else{
                JOptionPane.showMessageDialog(null, "please choose two cards first.");
            }
            repaint();
        }
    }

    /**
     * @return  canvasWidth for playingDeck
     */
    public static int getCanvasWidth(){
        return canvasPanel.getWidth();
    }

    /**
     * @return canvasHeight for playingDeck
     * @return
     */
    public static int getCanvasHeight(){
        return canvasPanel.getHeight();
    }

    /**
     * CanvasPanel is the class upon which we actually draw.  It listens
     * for mouse events.
     */
    private class CanvasPanel extends JPanel implements MouseListener, MouseMotionListener {
        private static final long serialVersionUID = 0;

        /**
         * Constructor just needs to set up the CanvasPanel as a listener.
         */
        public CanvasPanel() {
            addMouseListener(this);
            addMouseMotionListener(this);

        }

        /**
         * Paint the whole drawing.
         * if it is in tutorial, printout the tutorial message.
         * @page the Graphics object to draw on
         */
        public void paintComponent(Graphics page) {
            super.paintComponent(page);
            myGame.display(page);
        }

        /**
         * When the mouse is clicked, check to see if setgame can remove card and if the game ends.
         */
        public void mouseClicked(MouseEvent event) {
        }

        //we don't use these.
        public void mousePressed(MouseEvent event) {
            int result = myGame.getCards(event.getPoint());
            if (result == 1){
                remove = true;
            }else if (result == 0){
                turnUpdate();
                remove = false;
            }else{
                turnCount++;
                remove = false;
            }
            repaint();
        }

        public void mouseDragged(MouseEvent event)  { }
        public void mouseReleased(MouseEvent event) { }
        public void mouseEntered(MouseEvent event) { }
        public void mouseExited(MouseEvent event) { }
        public void mouseMoved(MouseEvent event) { }
    }
}
