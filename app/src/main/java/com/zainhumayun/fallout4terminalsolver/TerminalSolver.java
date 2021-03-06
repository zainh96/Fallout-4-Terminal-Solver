package com.zainhumayun.fallout4terminalsolver;

import com.zainhumayun.fallout4terminalsolver.models.WordFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Container class that solves a Fallout Terminal puzzle
 **/
public class TerminalSolver {
    private List<String> wordsLeft;
    private List<String> backUp;
    private int wordSize = 0;
    private SolverListener listener = null;
    private Stack<History> historyStack = new Stack<>();

    // difficulty enums
    public enum Difficulty {
        NOVICE,
        ADVANCED,
        EXPERT,
        MASTER,
        UNKNOWN
    }

    // min word length for novice
    private static final int NOVICE_MIN_LENGTH = 4;

    // max word length for novice
    private static final int NOVICE_MAX_LENGTH = 5;

    // max word length for advanced
    private static final int ADVANCED_MAX_LENGTH = 8;

    // max word length for expert
    private static final int EXPERT_MAX_LENGTH = 10;

    // max word length for master
    private static final int MASTER_MAX_LENGTH = 12;

    public interface SolverListener {
        void onTerminalSolverFinished(String solvedWord);
        void onUndoApplied(List<String> removedWords, WordFilter filter);
        void onFilterApplied(List<String> removedWords, WordFilter filter);
        void onRestarted(List<String> words);
    }

    /**
     * Pass in the full list of words here
     **/
    public TerminalSolver(List<String> wordsLeft){
        this.wordsLeft = wordsLeft;
        this.backUp = new ArrayList<>(wordsLeft);
        this.wordSize = wordsLeft.get(0).length();
    }

    public void setSolverListener(SolverListener listener){
        this.listener = listener;
    }

    public void applyFilter(WordFilter filter){
        History historyItem = new History(filter);

        if(filter.getLikeness() == wordSize){
            for(String word : wordsLeft){
                if(!word.equals(filter.getWord()))
                    historyItem.addRemovedWord(word);
            }

            wordsLeft = new ArrayList<>();
            wordsLeft.add(filter.getWord());
        } else {
            Iterator<String> candidatesIterator = wordsLeft.iterator();

            while (candidatesIterator.hasNext()) {
                String word = candidatesIterator.next();
                if (!shouldKeep(filter, word)) {
                    historyItem.addRemovedWord(word);
                    candidatesIterator.remove();
                }
            }

            if(wordSize != filter.getLikeness()) {
                wordsLeft.remove(filter.getWord());
            }
        }

        historyStack.push(historyItem);

        if(listener != null){
            listener.onFilterApplied(historyItem.removedWords, filter);
            if(isSolved() || couldNotSolve()) {
                listener.onTerminalSolverFinished(getSolvedWord());
            }
        }
    }

    private boolean shouldKeep(WordFilter filter, String word){
        int similarChars = getNumberOfCharacterMatches(filter.getWord(), word);
        return similarChars == filter.getLikeness() || filter.getWord().equals(word);
    }

    private int getNumberOfCharacterMatches(String s1, String s2){
        int similarChars = 0;
        for(int i = 0; i < s1.length(); i++){
            if(s1.charAt(i) == s2.charAt(i))
                similarChars++;
        }
        return similarChars;
    }

    public boolean isSolved(){
        return wordsLeft.size() == 1;
    }

    public String getSolvedWord(){
        if(!isSolved())
            return null;
        return wordsLeft.get(0);
    }

    public boolean couldNotSolve(){
        return wordsLeft.size() == 0;
    }

    public int getNumWordsLeft(){
        return wordsLeft.size();
    }

    public int getHistoryDepth(){
        return historyStack.size();
    }

    public int getLikeness(){
        return this.wordSize;
    }

    public void undo(){
        if(getHistoryDepth() == 0)
            return;

        History historyItem = historyStack.pop();

        wordsLeft.addAll(historyItem.removedWords);
        wordsLeft.add(historyItem.filter.getWord());

        if(listener != null){
            listener.onUndoApplied(historyItem.removedWords, historyItem.filter);
        }
    }

    public void restart(){
        wordsLeft = new ArrayList<>(backUp);
        historyStack = new Stack<>();

        if(listener != null){
            listener.onRestarted(wordsLeft);
        }
    }

    public boolean haveAppliedFilters(){
        return getHistoryDepth() != 0;
    }

    public Difficulty getDifficulty(){
        if(wordSize < NOVICE_MIN_LENGTH || wordSize > MASTER_MAX_LENGTH)
            return Difficulty.UNKNOWN;
        else if(wordSize <= NOVICE_MAX_LENGTH)
            return Difficulty.NOVICE;
        else if(wordSize <= ADVANCED_MAX_LENGTH)
            return Difficulty.ADVANCED;
        else if(wordSize <= EXPERT_MAX_LENGTH)
            return Difficulty.EXPERT;
        else if (wordSize <= MASTER_MAX_LENGTH)
            return Difficulty.MASTER;
        else
            return Difficulty.UNKNOWN;
    }

    private static class History {
        private WordFilter filter;
        private List<String> removedWords = new ArrayList<>();

        public History(WordFilter filter){
            this.filter = filter;
        }

        public void addRemovedWord(String word){
            removedWords.add(word);
        }
    }
}