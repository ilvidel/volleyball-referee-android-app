package com.tonkar.volleyballreferee.ui.game;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionListener;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import java.util.HashMap;
import java.util.Map;

public abstract class CourtFragment extends Fragment implements TeamListener, SanctionListener {

    protected       View                      mView;
    protected       TeamService               mTeamService;
    protected       SanctionService           mSanctionService;
    protected       TeamType                  mTeamOnLeftSide;
    protected       TeamType                  mTeamOnRightSide;
    protected final Map<PositionType, Button> mLeftTeamPositions;
    protected final Map<PositionType, Button> mRightTeamPositions;

    public CourtFragment() {
        mLeftTeamPositions = new HashMap<>();
        mRightTeamPositions = new HashMap<>();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTeamService.removeTeamListener(this);
        mSanctionService.removeSanctionListener(this);
        mLeftTeamPositions.clear();
        mRightTeamPositions.clear();
    }

    protected void initView() {
        Log.i("VBR-Court", "Create court fragment");
        mTeamService = ServicesProvider.getInstance().getTeamService();
        mSanctionService = ServicesProvider.getInstance().getSanctionService();

        mTeamOnLeftSide = mTeamService.getTeamOnLeftSide();
        mTeamOnRightSide = mTeamService.getTeamOnRightSide();
        mTeamService.addTeamListener(this);
        mSanctionService.addSanctionListener(this);
    }

    protected void addButtonOnLeftSide(final PositionType positionType, final Button button) {
        mLeftTeamPositions.put(positionType, button);
    }

    protected void addButtonOnRightSide(final PositionType positionType, final Button button) {
        mRightTeamPositions.put(positionType, button);
    }

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mTeamOnLeftSide = leftTeamType;
        mTeamOnRightSide = rightTeamType;
    }
}
