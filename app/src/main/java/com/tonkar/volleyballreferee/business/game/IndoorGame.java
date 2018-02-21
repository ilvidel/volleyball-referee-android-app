package com.tonkar.volleyballreferee.business.game;

import com.tonkar.volleyballreferee.business.team.IndoorTeamComposition;
import com.tonkar.volleyballreferee.business.team.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.sanction.Sanction;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class IndoorGame extends Game implements IndoorTeamService {

    public IndoorGame(final Rules rules) {
        super(GameType.INDOOR, rules);
    }

    // For GSON Deserialization
    public IndoorGame() {
        this(Rules.OFFICIAL_INDOOR_RULES);
    }

    @Override
    protected TeamDefinition createTeamDefinition(TeamType teamType) {
        return new IndoorTeamDefinition(teamType);
    }

    @Override
    protected Set createSet(Rules rules, boolean isTieBreakSet, TeamType servingTeamAtStart) {
        return new IndoorSet(rules, isTieBreakSet ? 15 : rules.getPointsPerSet(), servingTeamAtStart);
    }

    private IndoorTeamDefinition getIndoorTeamDefinition(TeamType teamType) {
        return (IndoorTeamDefinition) getTeamDefinition(teamType);
    }

    private IndoorTeamComposition getIndoorTeamComposition(TeamType teamType) {
        return (IndoorTeamComposition) currentSet().getTeamComposition(teamType);
    }

    @Override
    public void addPoint(final TeamType teamType) {
        super.addPoint(teamType);

        if (!currentSet().isSetCompleted()) {
            checkPosition1(teamType);

            final int leadingScore = currentSet().getPoints(currentSet().getLeadingTeam());

            // In indoor volley, the teams change sides after the 8th during the tie break
            if (isTieBreakSet() && leadingScore == 8 && currentSet().getPoints(TeamType.HOME) != currentSet().getPoints(TeamType.GUEST)) {
                swapTeams(ActionOriginType.APPLICATION);
            }

            // In indoor volley, there are two technical timeouts at 8 and 16 but not during tie break
            if (getRules().areTechnicalTimeoutsEnabled()
                    && !isTieBreakSet()
                    && currentSet().getLeadingTeam().equals(teamType)
                    && (leadingScore == 8 || leadingScore == 16)
                    && currentSet().getPoints(TeamType.HOME) != currentSet().getPoints(TeamType.GUEST)) {
                notifyTechnicalTimeoutReached();
            }

            // Specific custom rule
            if (samePlayerServedNConsecutiveTimes(teamType, getPoints(teamType), getPointsLadder())) {
                rotateToNextPositions(teamType);
            }
        }
    }

    @Override
    public void removeLastPoint() {
        TeamType oldServingTeam = getServingTeam();
        super.removeLastPoint();

        TeamType newServingTeam = getServingTeam();
        checkPosition1(newServingTeam);

        final int leadingScore = currentSet().getPoints(currentSet().getLeadingTeam());

        // In indoor volley, the teams change sides after the 8th during the tie break
        if (isTieBreakSet() && leadingScore == 7) {
            swapTeams(ActionOriginType.APPLICATION);
        }

        // Specific custom rule
        if (oldServingTeam.equals(newServingTeam) && samePlayerHadServedNConsecutiveTimes(oldServingTeam, getPoints(oldServingTeam), getPointsLadder())) {
            rotateToPreviousPositions(oldServingTeam);
        }
    }

    private void checkPosition1(final TeamType scoringTeam) {
        int number = getIndoorTeamComposition(scoringTeam).checkPosition1Offence();
        if (number > 0)  {
            substitutePlayer(scoringTeam, number, PositionType.POSITION_1, ActionOriginType.APPLICATION);
        }

        TeamType defendingTeam = scoringTeam.other();
        number = getIndoorTeamComposition(defendingTeam).checkPosition1Defence();
        if (number > 0)  {
            substitutePlayer(defendingTeam, number, PositionType.POSITION_1, ActionOriginType.APPLICATION);
        }
    }

    @Override
    public void substitutePlayer(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        if (getIndoorTeamComposition(teamType).substitutePlayer(number, positionType, getPoints(TeamType.HOME), getPoints(TeamType.GUEST))) {
            notifyPlayerChanged(teamType, number, positionType, actionOriginType);
        }
    }

    @Override
    public java.util.Set<Integer> getPossibleSubstitutions(TeamType teamType, PositionType positionType) {
        return getIndoorTeamComposition(teamType).getPossibleSubstitutions(positionType);
    }

    @Override
    public void confirmStartingLineup() {
        getIndoorTeamComposition(TeamType.HOME).confirmStartingLineup();
        getIndoorTeamComposition(TeamType.GUEST).confirmStartingLineup();
    }

    @Override
    public boolean hasActingCaptainOnCourt(TeamType teamType) {
        return getIndoorTeamComposition(teamType).hasActingCaptainOnCourt();
    }

    @Override
    public int getActingCaptain(TeamType teamType, int setIndex) {
        int number = -1;

        Set set = getSet(setIndex);

        if (set != null) {
            IndoorTeamComposition indoorTeamComposition = (IndoorTeamComposition) set.getTeamComposition(teamType);
            number = indoorTeamComposition.getActingCaptain();
        }

        return number;
    }

    @Override
    public void setActingCaptain(TeamType teamType, int number) {
        getIndoorTeamComposition(teamType).setActingCaptain(number);
    }

    @Override
    public boolean isActingCaptain(TeamType teamType, int number) {
        return getIndoorTeamComposition(teamType).isActingCaptain(number);
    }

    @Override
    public java.util.Set<Integer> getPossibleActingCaptains(TeamType teamType) {
        return getIndoorTeamComposition(teamType).getPossibleActingCaptains();
    }

    @Override
    public boolean hasRemainingSubstitutions(TeamType teamType) {
        return getIndoorTeamComposition(teamType).canSubstitute();
    }

    @Override
    public java.util.Set<Integer> filterSubstitutionsWithExpulsedOrDisqualifiedPlayersForCurrentSet(TeamType teamType, int excludedNumber, java.util.Set<Integer> possibleSubstitutions) {
        final java.util.Set<Integer> filteredSubstitutions = new HashSet<>(possibleSubstitutions);
        final java.util.Set<Integer> excludedNumbers = getExpulsedOrDisqualifiedPlayersForCurrentSet(teamType);

        for(Iterator<Integer> iterator = filteredSubstitutions.iterator(); iterator.hasNext();) {
            int possibleReplacement = iterator.next();
            if (excludedNumbers.contains(possibleReplacement)) {
                iterator.remove();
            } else if (!isLibero(teamType, excludedNumber) && isLibero(teamType, possibleReplacement)) {
                iterator.remove();
            }
        }

        return filteredSubstitutions;
    }

    @Override
    public boolean isStartingLineupConfirmed() {
        return getIndoorTeamComposition(TeamType.HOME).isStartingLineupConfirmed() && getIndoorTeamComposition(TeamType.GUEST).isStartingLineupConfirmed();
    }

    @Override
    public java.util.Set<Integer> getPlayersInStartingLineup(TeamType teamType, int setIndex) {
        java.util.Set<Integer> players = new TreeSet<>();

        Set set = getSet(setIndex);

        if (set != null) {
            IndoorTeamComposition indoorTeamComposition = (IndoorTeamComposition) set.getTeamComposition(teamType);
            players = indoorTeamComposition.getPlayersInStartingLineup();
        }

        return players;
    }

    @Override
    public PositionType getPlayerPositionInStartingLineup(TeamType teamType, int number, int setIndex) {
        PositionType positionType = null;

        Set set = getSet(setIndex);

        if (set != null) {
            IndoorTeamComposition indoorTeamComposition = (IndoorTeamComposition) set.getTeamComposition(teamType);
            positionType = indoorTeamComposition.getPlayerPositionInStartingLineup(number);
        }

        return positionType;
    }

    @Override
    public int getPlayerAtPositionInStartingLineup(TeamType teamType, PositionType positionType, int setIndex) {
        int number = -1;

        Set set = getSet(setIndex);

        if (set != null) {
            IndoorTeamComposition indoorTeamComposition = (IndoorTeamComposition) set.getTeamComposition(teamType);
            number = indoorTeamComposition.getPlayerAtPositionInStartingLineup(positionType);
        }

        return number;
    }

    @Override
    public int getLiberoColor(TeamType teamType) {
        return getIndoorTeamDefinition(teamType).getLiberoColor();
    }

    @Override
    public void setLiberoColor(TeamType teamType, int color) {
        getIndoorTeamDefinition(teamType).setLiberoColor(color);
    }

    @Override
    public void addLibero(TeamType teamType, int number) {
        getIndoorTeamDefinition(teamType).addLibero(number);
    }

    @Override
    public void removeLibero(TeamType teamType, int number) {
        getIndoorTeamDefinition(teamType).removeLibero(number);
    }

    @Override
    public boolean isLibero(TeamType teamType, int number) {
        return getIndoorTeamDefinition(teamType).isLibero(number);
    }

    @Override
    public boolean canAddLibero(TeamType teamType) {
        return getIndoorTeamDefinition(teamType).canAddLibero();
    }

    @Override
    public java.util.Set<Integer> getLiberos(TeamType teamType) {
        return getIndoorTeamDefinition(teamType).getLiberos();
    }

    @Override
    public List<Substitution> getSubstitutions(TeamType teamType) {
        return getIndoorTeamComposition(teamType).getSubstitutions();
    }

    @Override
    public List<Substitution> getSubstitutions(TeamType teamType, int setIndex) {
        List<Substitution> substitutions = new ArrayList<>();

        Set set = getSet(setIndex);

        if (set != null) {
            IndoorTeamComposition indoorTeamComposition = (IndoorTeamComposition) set.getTeamComposition(teamType);
            substitutions = indoorTeamComposition.getSubstitutions();
        }

        return substitutions;
    }

    @Override
    public void setCaptain(TeamType teamType, int number) {
        getIndoorTeamDefinition(teamType).setCaptain(number);
    }

    @Override
    public int getCaptain(TeamType teamType) {
        return getIndoorTeamDefinition(teamType).getCaptain();
    }

    @Override
    public java.util.Set<Integer> getPossibleCaptains(TeamType teamType) {
        return getIndoorTeamDefinition(teamType).getPossibleCaptains();
    }

    @Override
    public boolean isCaptain(TeamType teamType, int number) {
        return getIndoorTeamDefinition(teamType).isCaptain(number);
    }

    @Override
    public void giveSanction(TeamType teamType, SanctionType sanctionType, int number) {
        super.giveSanction(teamType, sanctionType, number);

        if (number > 0 && (SanctionType.RED_EXPULSION.equals(sanctionType) || SanctionType.RED_DISQUALIFICATION.equals(sanctionType))) {
            // The player excluded for the set/match has to be legally replaced
            PositionType positionType = getPlayerPosition(teamType, number);

            if (!PositionType.BENCH.equals(positionType)) {
                final java.util.Set<Integer> possibleSubstitutions = getPossibleSubstitutions(teamType, positionType);
                final java.util.Set<Integer> filteredSubstitutions = filterSubstitutionsWithExpulsedOrDisqualifiedPlayersForCurrentSet(teamType, number, possibleSubstitutions);

                // If there is no possible legal substituion, the set is lost
                if (filteredSubstitutions.size() == 0) {
                    forceFinishSet(teamType.other());
                }
            }
        }

        if (SanctionType.RED_DISQUALIFICATION.equals(sanctionType) && !isMatchCompleted()) {
            // check that the team has enough players to continue the match
            java.util.Set<Integer> players = getIndoorTeamDefinition(teamType).getPlayers();

            // first remove the liberos

            for (int libero : getIndoorTeamDefinition(teamType).getLiberos()) {
                players.remove(libero);
            }

            // then remove the disqualified players

            for (Sanction sanction : getGivenSanctions(teamType)) {
                if (SanctionType.RED_DISQUALIFICATION.equals(sanction.getSanctionType())) {
                    players.remove(sanction.getPlayer());
                }
            }

            if (players.size() < 6) {
                // not enought players: finish the match
                forceFinishMatch(teamType.other());
            }
        }
    }

    /* *******************************
     * Specific custom rules section *
     * *******************************/

    public boolean samePlayerServedNConsecutiveTimes(TeamType teamType, int teamPoints, List<TeamType> pointsLadder) {
        boolean result = false;

        int limit = getRules().getCustomConsecutiveServesPerPlayer();
        if (limit <= teamPoints) {
            int consecutiveServes = getConsecutiveServes(teamType, pointsLadder);

            if (consecutiveServes > 0 && consecutiveServes % limit == 0) {
                result = true;
            }
        }

        return result;
    }

    public boolean samePlayerHadServedNConsecutiveTimes(TeamType teamType, int teamPoints, List<TeamType> pointsLadder) {
        List<TeamType> tempPointsLadder = new ArrayList<>(pointsLadder);
        tempPointsLadder.add(teamType);
        int tempTeamPoints = teamPoints + 1;
        return samePlayerServedNConsecutiveTimes(teamType, tempTeamPoints, tempPointsLadder);
    }

    private int getConsecutiveServes(TeamType teamType, List<TeamType> pointsLadder) {
        int consecutiveServes;
        int ladderIndex = pointsLadder.size() - 1;

        if (pointsLadder.isEmpty()) {
            consecutiveServes = 0;
        } else if (teamType.equals(pointsLadder.get(ladderIndex))) {
            List<TeamType> consecutivePoints = new ArrayList<>();
            while (ladderIndex >= 0 && teamType.equals(pointsLadder.get(ladderIndex))) {
                consecutivePoints.add(teamType);
                ladderIndex--;
            }
            consecutiveServes = consecutivePoints.size();

            // Side-out doesn't count as a serve
            if (ladderIndex >= 0 && !teamType.equals(pointsLadder.get(ladderIndex))) {
                consecutiveServes--;
            } else if (ladderIndex < 0 && !currentSet().getServingTeamAtStart().equals(teamType)) {
                consecutiveServes--;
            }
        } else {
            consecutiveServes = 0;
        }

        return consecutiveServes;
    }
}
