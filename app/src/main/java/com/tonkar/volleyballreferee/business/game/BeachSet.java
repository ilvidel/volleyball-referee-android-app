package com.tonkar.volleyballreferee.business.game;

import com.tonkar.volleyballreferee.business.team.BeachTeamComposition;
import com.tonkar.volleyballreferee.business.team.TeamComposition;
import com.tonkar.volleyballreferee.business.team.TeamDefinition;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.rules.Rules;

public class BeachSet extends Set {

    public BeachSet(Rules rules, int pointsToWinSet, TeamType servingTeamAtStart) {
        super(rules, pointsToWinSet, servingTeamAtStart);
    }

    @Override
    protected TeamComposition createTeamComposition(Rules rules, TeamDefinition teamDefinition) {
        return new BeachTeamComposition(teamDefinition);
    }

}