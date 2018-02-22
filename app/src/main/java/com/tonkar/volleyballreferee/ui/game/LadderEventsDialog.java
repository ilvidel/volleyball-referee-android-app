package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.sanction.Sanction;
import com.tonkar.volleyballreferee.interfaces.team.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.List;

public class LadderEventsDialog {

    private       AlertDialog    mAlertDialog;
    private final LayoutInflater mLayoutInflater;
    private final Context        mContext;
    private final TeamType       mTeamType;
    private final BaseTeamService mBaseTeamService;

    LadderEventsDialog(LayoutInflater layoutInflater, final Context context, final TeamType teamType, final LadderItem ladderItem, final BaseTeamService baseTeamService) {
        mLayoutInflater = layoutInflater;
        mContext = context;
        mTeamType = teamType;
        mBaseTeamService = baseTeamService;

        View view = layoutInflater.inflate(R.layout.ladder_events_dialog, null);

        ListView timeoutList = view.findViewById(R.id.timeout_list);
        TimeoutsListAdapter timeoutsListAdapter = new TimeoutsListAdapter(TeamType.HOME.equals(mTeamType) ? ladderItem.getHomeTimeouts().size() > 0 : ladderItem.getGuestTimeouts().size() > 0);
        timeoutList.setAdapter(timeoutsListAdapter);

        ListView substitutionList = view.findViewById(R.id.substitution_list);
        SubstitutionsListAdapter substitutionsListAdapter = new SubstitutionsListAdapter(TeamType.HOME.equals(mTeamType) ? ladderItem.getHomeSubstitutions() : ladderItem.getGuestSubstitutions());
        substitutionList.setAdapter(substitutionsListAdapter);

        ListView cardList = view.findViewById(R.id.sanction_list);
        SanctionsListAdapter sanctionsListAdapter = new SanctionsListAdapter(TeamType.HOME.equals(mTeamType) ? ladderItem.getHomeSanctions() : ladderItem.getGuestSanctions());
        cardList.setAdapter(sanctionsListAdapter);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
        builder.setView(view);

        mAlertDialog = builder.create();
    }

    public void show() {
        if (mAlertDialog != null) {
            mAlertDialog.show();
        }
    }

    private class TimeoutsListAdapter extends BaseAdapter {

        private boolean mHasTimeout;

        private TimeoutsListAdapter(boolean hasTimeout) {
            mHasTimeout = hasTimeout;
        }

        @Override
        public int getCount() {
            return mHasTimeout ? 1 : 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int index, View view, ViewGroup viewGroup) {
            ImageView imageView = (ImageView) view;

            if (imageView == null) {
                imageView = (ImageView) mLayoutInflater.inflate(R.layout.ladder_event_item, null);
            }

            imageView.setImageResource(R.drawable.ic_timeout);
            imageView.getDrawable().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryText), PorterDuff.Mode.SRC_IN));

            return imageView;
        }
    }

    private class SubstitutionsListAdapter extends BaseAdapter {

        private List<Substitution> mSubstitutions;

        private SubstitutionsListAdapter(List<Substitution> substitutions) {
            mSubstitutions = substitutions;
        }

        @Override
        public int getCount() {
            return mSubstitutions.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int index, View view, ViewGroup viewGroup) {
            View substitutionView = view;

            if (substitutionView == null) {
                substitutionView = mLayoutInflater.inflate(R.layout.substitution_list_item, null);
            }

            TextView scoreText = substitutionView.findViewById(R.id.score_text);
            scoreText.setVisibility(View.GONE);
            TextView playerInText = substitutionView.findViewById(R.id.player_in_text);
            TextView playerOutText = substitutionView.findViewById(R.id.player_out_text);

            Substitution substitution = mSubstitutions.get(index);
            playerInText.setText(String.valueOf(substitution.getPlayerIn()));
            playerOutText.setText(String.valueOf(substitution.getPlayerOut()));

            UiUtils.styleBaseTeamText(mContext, mBaseTeamService, mTeamType, playerInText);
            UiUtils.styleBaseTeamText(mContext, mBaseTeamService, mTeamType, playerOutText);

            return substitutionView;
        }
    }

    private class SanctionsListAdapter extends BaseAdapter {

        private List<Sanction> mSanctions;

        private SanctionsListAdapter(List<Sanction> sanctions) {
            mSanctions = sanctions;
        }

        @Override
        public int getCount() {
            return mSanctions.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int index, View view, ViewGroup viewGroup) {
            View sanctionView = view;

            if (sanctionView == null) {
                sanctionView = mLayoutInflater.inflate(R.layout.sanction_list_item, null);
            }

            TextView setText = sanctionView.findViewById(R.id.set_text);
            setText.setVisibility(View.GONE);
            TextView scoreText = sanctionView.findViewById(R.id.score_text);
            scoreText.setVisibility(View.GONE);
            TextView playerText = sanctionView.findViewById(R.id.player_text);
            ImageView sanctionTypeImage = sanctionView.findViewById(R.id.sanction_type_image);

            Sanction sanction = mSanctions.get(index);

            switch (sanction.getSanctionType()) {
                case YELLOW:
                    sanctionTypeImage.setImageResource(R.drawable.yellow_card);
                    break;
                case RED:
                    sanctionTypeImage.setImageResource(R.drawable.red_card);
                    break;
                case RED_EXPULSION:
                    sanctionTypeImage.setImageResource(R.drawable.expulsion_card);
                    break;
                case RED_DISQUALIFICATION:
                    sanctionTypeImage.setImageResource(R.drawable.disqualification_card);
                    break;
                case DELAY_WARNING:
                    sanctionTypeImage.setImageResource(R.drawable.delay_warning);
                    break;
                case DELAY_PENALTY:
                    sanctionTypeImage.setImageResource(R.drawable.delay_penalty);
                    break;
            }

            if (sanction.getPlayer() < 0) {
                playerText.setVisibility(View.GONE);
            } else {
                if (sanction.getPlayer() > 0) {
                    playerText.setText(String.valueOf(sanction.getPlayer()));
                } else {
                    playerText.setText(mContext.getResources().getString(R.string.coach_abbreviation));
                }

                if (mBaseTeamService instanceof BaseIndoorTeamService) {
                    UiUtils.styleBaseIndoorTeamText(mContext, (BaseIndoorTeamService) mBaseTeamService, mTeamType, sanction.getPlayer(), playerText);
                } else {
                    UiUtils.styleBaseTeamText(mContext, mBaseTeamService, mTeamType, playerText);
                }
            }

            return sanctionView;
        }
    }
}