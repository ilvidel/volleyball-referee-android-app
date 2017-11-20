package com.tonkar.volleyballreferee.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.business.game.Game;
import com.tonkar.volleyballreferee.business.game.GameFactory;
import com.tonkar.volleyballreferee.business.history.GamesHistory;
import com.tonkar.volleyballreferee.interfaces.GamesHistoryService;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.history.RecentGamesListActivity;
import com.tonkar.volleyballreferee.ui.rules.RulesActivity;
import com.tonkar.volleyballreferee.ui.team.QuickTeamsSetupActivity;
import com.tonkar.volleyballreferee.ui.team.TeamsSetupActivity;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class MainActivity extends AppCompatActivity {

    private static final int  PERMISSIONS_REQUEST_WRITE_STORAGE = 1;

    private GamesHistoryService mGamesHistoryService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("VBR-MainActivity", "Create main activity");

        PreferenceManager.setDefaultValues(this, R.xml.rules, false);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            AlertDialogFragment alertDialogFragment;

            if (savedInstanceState == null) {
                alertDialogFragment = AlertDialogFragment.newInstance(getResources().getString(R.string.permission_title), getResources().getString(R.string.permission_message),
                        getResources().getString(android.R.string.ok));
                alertDialogFragment.show(getFragmentManager(), "permission");
            }
            else {
                alertDialogFragment = (AlertDialogFragment) getFragmentManager().findFragmentByTag("permission");
            }

            if (alertDialogFragment != null) {
                alertDialogFragment.setAlertDialogListener(new AlertDialogFragment.AlertDialogListener() {
                    @Override
                    public void onNegativeButtonClicked() {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_STORAGE);
                    }

                    @Override
                    public void onPositiveButtonClicked() {
                    }

                    @Override
                    public void onNeutralButtonClicked() {
                    }
                });
            }
        }

        mGamesHistoryService = new GamesHistory(getApplicationContext());
        mGamesHistoryService.loadRecordedGames();
        ServicesProvider.getInstance().setGameHistoryService(mGamesHistoryService);

        resumeCurrentGameWithDialog(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem importantMessageItem = menu.findItem(R.id.action_important_message);
        importantMessageItem.setVisible(mGamesHistoryService.hasCurrentGame());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_rules:
                Log.i("VBR-MainActivity", "Edit rules");
                Intent intent = new Intent(this, RulesActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_recent_games:
                Log.i("VBR-MainActivity", "Recent games");
                intent = new Intent(this, RecentGamesListActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_important_message:
                Log.i("VBR-MainActivity", "Resume game");
                resumeCurrentGameWithDialog(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startOfficialIndoorGame(View view) {
        Log.i("VBR-MainActivity", "Start an official indoor game");
        startIndoorGame(false);
    }

    public void startCustomIndoorGame(View view) {
        Log.i("VBR-MainActivity", "Start a custom indoor game");
        startIndoorGame(true);
    }

    public void startOfficialBeachGame(View view) {
        Log.i("VBR-MainActivity", "Start an official beach game");
        startBeachGame(false);
    }

    public void startCustomBeachGame(View view) {
        Log.i("VBR-MainActivity", "Start a custom beach game");
        startBeachGame(true);
    }

    private void startIndoorGame(final boolean custom) {
        final boolean teamOf6Players;
        final Game game;

        if (custom) {
            game = GameFactory.createIndoorGame(PreferenceManager.getDefaultSharedPreferences(this));

            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            teamOf6Players = sharedPreferences.getBoolean("pref_players_number", Rules.OFFICIAL_INDOOR_RULES.isTeamOf6Players());
        } else {
            game = GameFactory.createIndoorGame();
            teamOf6Players = Rules.OFFICIAL_INDOOR_RULES.isTeamOf6Players();
        }

        initServices(game);

        if (teamOf6Players) {
            Log.i("VBR-MainActivity", String.format("Start activity to setup %s team", TeamType.HOME.toString()));
            final Intent intent = new Intent(this, TeamsSetupActivity.class);
            startActivity(intent);
        } else {
            Log.i("VBR-MainActivity", "Start activity to setup both teams");
            final Intent intent = new Intent(this, QuickTeamsSetupActivity.class);
            startActivity(intent);
        }
    }

    private void startBeachGame(final boolean custom) {
        final Game game;

        if (custom) {
            game = GameFactory.createBeachGame(PreferenceManager.getDefaultSharedPreferences(this));
        } else {
            game = GameFactory.createBeachGame();
        }

        initServices(game);

        Log.i("VBR-MainActivity", "Start activity to setup both teams");
        final Intent intent = new Intent(this, QuickTeamsSetupActivity.class);
        startActivity(intent);
    }

    private void initServices(Game game) {
        ServicesProvider.getInstance().setGameService(game);
        ServicesProvider.getInstance().setTeamService(game);
        ServicesProvider.getInstance().setTimeoutService(game);
    }

    private void resumeCurrentGameWithDialog(Bundle savedInstanceState) {
        boolean autoIgnore = getIntent().getBooleanExtra("auto_ignore_resume_game", false);
        getIntent().removeExtra("auto_ignore_resume_game");

        if (mGamesHistoryService.hasCurrentGame() && !autoIgnore) {
            AlertDialogFragment alertDialogFragment;

            if (savedInstanceState == null) {
                alertDialogFragment = AlertDialogFragment.newInstance(getResources().getString(R.string.resume_game_title), getResources().getString(R.string.resume_game_question),
                        getResources().getString(R.string.delete), getResources().getString(R.string.resume), getResources().getString(R.string.ignore));
                alertDialogFragment.show(getFragmentManager(), "current_game");
            }
            else {
                alertDialogFragment = (AlertDialogFragment) getFragmentManager().findFragmentByTag("current_game");
            }

            if (alertDialogFragment != null) {
                alertDialogFragment.setAlertDialogListener(new AlertDialogFragment.AlertDialogListener() {
                    @Override
                    public void onNegativeButtonClicked() {
                        Log.i("VBR-MainActivity", "Delete current game");
                        mGamesHistoryService.deleteCurrentGame();
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.deleted_game), Toast.LENGTH_LONG).show();
                        invalidateOptionsMenu();
                    }

                    @Override
                    public void onPositiveButtonClicked() {
                        Log.i("VBR-MainActivity", "Resume current game");
                        mGamesHistoryService.resumeCurrentGame();

                        Log.i("VBR-MainActivity", "Start game activity");
                        final Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                        startActivity(gameIntent);
                    }

                    @Override
                    public void onNeutralButtonClicked() {
                    }
                });
            }
        }
    }
}
