package pacman;

import java.util.ArrayList;

import java.util.Collections;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Pacman {
	
	private static final String PACMAN = "actor0";
	private static final String CONTROLS = "pcm-c";
	
	private static final int MIN_PAUSE = 500;
	private static final int MAX_PAUSE = 2000;
	
	private static Random random = new Random();
	private static int move;
	private static ArrayList<Keys> moves = new ArrayList<Keys>();
	private static ArrayList<Integer> highScores = new ArrayList<Integer>();
	private static WebElement controls;
	private static WebDriver driver;
	
	public static void main(String [ ] args) throws InterruptedException {
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		
		while (true) {
			playPacman();
		}
	}
	
	private static void playPacman() throws InterruptedException {
		driver.get("http://www.google.com/pacman/");
		controls = driver.findElement(By.id(CONTROLS));
		
		ArrayList<Keys> potentialDirections = new ArrayList<Keys>();
		move = 1;
		
		while(!isPacmanAlive() && !isPacmanReady()) {
			Thread.sleep(500);
		}
		
		System.out.println("==================");
		System.out.println("    LET'S GO!!");
		System.out.println("==================");
		
		while(isPacmanAlive()) {
			
			if (move == 1) {
				potentialDirections.add(Keys.ARROW_LEFT);
				potentialDirections.add(Keys.ARROW_RIGHT);
				Keys direction = potentialDirections.get(random.nextInt(potentialDirections.size()));
				move(direction, 1000);
			} else {
				if (move == 2) {
					move(Keys.ARROW_UP, 1000);
					move(moves.get(0), 1000);
				} else {
					potentialDirections.clear();
					potentialDirections.add(Keys.ARROW_UP);
					potentialDirections.add(Keys.ARROW_DOWN);
					potentialDirections.add(Keys.ARROW_LEFT);
					potentialDirections.add(Keys.ARROW_RIGHT);
					potentialDirections.remove(moves.get(moves.size()-1));
					potentialDirections.remove(getOppositeDirection(moves.get(moves.size()-1)));
					
					if (isPacmanAtTopEdge()) {
						System.out.println("Can't go up!");
						potentialDirections.remove(Keys.ARROW_UP);
					}
					if (isPacmanAtBottomEdge()) {
						System.out.println("Can't go down!");
						potentialDirections.remove(Keys.ARROW_DOWN);
					}
					if (isPacmanAtLeftEdge()) {
						System.out.println("Can't go left!");
						potentialDirections.remove(Keys.ARROW_LEFT);
					}
					if (isPacmanAtRightEdge()) {
						System.out.println("Can't go right!");
						potentialDirections.remove(Keys.ARROW_RIGHT);
					}
					
					Keys direction = potentialDirections.get(random.nextInt(potentialDirections.size()));
					int pause = random.nextInt((MAX_PAUSE - MIN_PAUSE + 1) + MIN_PAUSE);
					move(direction, pause);
				}
			}
		}
		
		gameOver();
	}
	
	private static void move(Keys direction, int pause) {
		moves.add(direction);
		System.out.println(move + ": " + direction.name() + ", " + pause);
		controls.sendKeys(direction);
		try {
			Thread.sleep(pause);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		move++;
	}
	
	private static Keys getOppositeDirection(Keys direction) {
		if (direction == Keys.ARROW_UP) {
			return Keys.ARROW_DOWN;
		} else if (direction == Keys.ARROW_DOWN) {
			return Keys.ARROW_UP;
		} else if (direction == Keys.ARROW_LEFT) {
			return Keys.ARROW_RIGHT;
		} else {
			return Keys.ARROW_LEFT;
		}
	}
	
	private static boolean isPacmanAlive() {
		return getBackgroundPositionForId(PACMAN)[0] > -142;
	}
	
	private static boolean isPacmanReady() {
		return getBackgroundPositionForId(PACMAN)[0] == -2;
	}
	
	private static boolean isPacmanAtTopEdge() {
		return getPositionForId(PACMAN)[1] == 8;
	}
	
	private static boolean isPacmanAtBottomEdge() {
		return getPositionForId(PACMAN)[1] == 120;
	}
	
	private static boolean isPacmanAtLeftEdge() {
		return getPositionForId(PACMAN)[0] == 8;
	}
	
	private static boolean isPacmanAtRightEdge() {
		return getPositionForId(PACMAN)[0] == 448;
	}
	
	private static int[] getPositionForId(String id) {
		int[] position = new int[2];
		position[0] = new Integer(((JavascriptExecutor) driver).executeScript("return document.getElementById('" + id + "').style.left;").toString().split("px")[0]);
		position[1] = new Integer(((JavascriptExecutor) driver).executeScript("return document.getElementById('" + id + "').style.top;").toString().split("px")[0]);
		//System.out.println("Position of " + id + " is: " + position[0] + ", " + position[1]);
		return position;
	}
	
	private static int[] getBackgroundPositionForId(String id) {
		int[] position = new int[2];
		position[0] = new Integer(((JavascriptExecutor) driver).executeScript("return document.getElementById('" + id + "').style.backgroundPosition;").toString().split(" ")[0].split("px")[0]);
		position[1] = new Integer(((JavascriptExecutor) driver).executeScript("return document.getElementById('" + id + "').style.backgroundPosition;").toString().split(" ")[1].split("px")[0]);
		//System.out.println("Background position of " + id + " is: " + position[0] + ", " + position[1]);
		return position;
	}
	
	private static int getScore() {
		String[] score = new String[10];
		for (int i=0; i < score.length; i++) {
			int[] position = getBackgroundPositionForId("pcm-sc-1-" + i);
			
			if (position[1] == -2 && position[0] == -62) {
				//empty score digits 
				score[i] = "";
			} else {
				score[i] = new Integer(Math.abs((position[0] + 12) / 10)).toString();
			}
		}
		return new Integer(StringUtils.join(score));
	}

	private static void gameOver() {
		System.out.println("==================");
		System.out.println("    GAME OVER!");
		System.out.println("==================");
		checkHighScores(getScore());
	}
	
	private static void checkHighScores(int score) {
		Collections.sort(highScores);
		Collections.reverse(highScores);
		if (highScores.size() < 10) {
			highScores.add(score);
		} else if (score > highScores.get(9)) {
			highScores.remove(9);
			highScores.add(score);
			System.out.println("NEW HIGH SCORE!");
		}
		Collections.sort(highScores);
		Collections.reverse(highScores);
		System.out.println("Score: " + score);
		System.out.println("==================");
		System.out.println("   HIGH SCORES:");
		System.out.println("==================");
		for (int i=0; i < highScores.size(); i++) {
			System.out.println(i+1 + ": " + highScores.get(i));
		}
		System.out.println();
	}
	
}
