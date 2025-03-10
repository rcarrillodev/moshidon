package org.joinmastodon.android.fragments.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.joinmastodon.android.BuildConfig;
import org.joinmastodon.android.E;
import org.joinmastodon.android.MainActivity;
import org.joinmastodon.android.R;
import org.joinmastodon.android.api.session.AccountSession;
import org.joinmastodon.android.api.session.AccountSessionManager;
import org.joinmastodon.android.model.Instance;
import org.joinmastodon.android.model.viewmodel.ListItem;
import org.joinmastodon.android.ui.sheets.AccountSwitcherSheet;
import org.joinmastodon.android.ui.M3AlertDialogBuilder;
import org.joinmastodon.android.ui.utils.HideableSingleViewRecyclerAdapter;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.Nav;
import me.grishka.appkit.utils.MergeRecyclerAdapter;

public class SettingsMainFragment extends BaseSettingsFragment<Void>{
	private AccountSession account;
	private boolean loggedOut;
	private HideableSingleViewRecyclerAdapter bannerAdapter;
	private TextView updateText;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		account=AccountSessionManager.get(accountID);
		setTitle(R.string.settings);
		setSubtitle(account.getFullUsername());
		onDataLoaded(List.of(
				new ListItem<>(R.string.settings_behavior, 0, R.drawable.ic_fluent_settings_24_regular, this::onBehaviorClick),
				new ListItem<>(R.string.settings_display, 0, R.drawable.ic_fluent_color_24_regular, this::onDisplayClick),
				new ListItem<>(R.string.settings_privacy, 0, R.drawable.ic_fluent_shield_24_regular, this::onPrivacyClick),
				new ListItem<>(R.string.settings_notifications, 0, R.drawable.ic_fluent_alert_24_regular, this::onNotificationsClick),
				new ListItem<>(R.string.sk_settings_instance, 0, R.drawable.ic_fluent_server_24_regular, this::onInstanceClick),
				new ListItem<>(getString(R.string.about_app, getString(R.string.mo_app_name)), null, R.drawable.ic_fluent_info_24_regular, this::onAboutClick, null, 0, true),
				new ListItem<>(R.string.manage_accounts, 0, R.drawable.ic_fluent_person_swap_24_regular, this::onManageAccountsClick),
				new ListItem<>(R.string.log_out, 0, R.drawable.ic_fluent_sign_out_24_regular, this::onLogOutClick, R.attr.colorM3Error, false)
		));

		Instance instance=AccountSessionManager.getInstance().getInstanceInfo(account.domain);
		if(!instance.isAkkoma()){
			data.add(3, new ListItem<>(R.string.settings_filters, 0, R.drawable.ic_fluent_filter_24_regular, this::onFiltersClick));
		}

		if(BuildConfig.DEBUG || BuildConfig.BUILD_TYPE.equals("appcenterPrivateBeta")){
			data.add(0, new ListItem<>("Debug settings", null, R.drawable.ic_fluent_wrench_screwdriver_24_regular, i->Nav.go(getActivity(), SettingsDebugFragment.class, makeFragmentArgs()), null, 0, true));
		}

		AccountSession session=AccountSessionManager.get(accountID);
		session.reloadPreferences(null);
		session.updateAccountInfo();
		E.register(this);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		E.unregister(this);
	}

	@Override
	protected void doLoadData(int offset, int count){}

	@Override
	protected void onHidden(){
		super.onHidden();
		if(!loggedOut)
			account.savePreferencesIfPending();
	}

	@Override
	protected RecyclerView.Adapter<?> getAdapter(){
		View banner=getActivity().getLayoutInflater().inflate(R.layout.item_settings_banner, list, false);
		updateText=banner.findViewById(R.id.text);
		TextView bannerTitle=banner.findViewById(R.id.title);
		ImageView bannerIcon=banner.findViewById(R.id.icon);
		bannerAdapter=new HideableSingleViewRecyclerAdapter(banner);
		bannerAdapter.setVisible(false);

		bannerTitle.setText(R.string.app_update_ready);
		bannerIcon.setImageResource(R.drawable.ic_fluent_phone_update_24_regular);

		MergeRecyclerAdapter adapter=new MergeRecyclerAdapter();
		adapter.addAdapter(bannerAdapter);
		adapter.addAdapter(super.getAdapter());
		return adapter;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
	}

	private Bundle makeFragmentArgs(){
		Bundle args=new Bundle();
		args.putString("account", accountID);
		return args;
	}

	private void onBehaviorClick(ListItem<?> item_){
		Nav.go(getActivity(), SettingsBehaviorFragment.class, makeFragmentArgs());
	}

	private void onDisplayClick(ListItem<?> item_){
		Nav.go(getActivity(), SettingsDisplayFragment.class, makeFragmentArgs());
	}

	private void onPrivacyClick(ListItem<?> item_){
		Nav.go(getActivity(), SettingsPrivacyFragment.class, makeFragmentArgs());
	}

	private void onFiltersClick(ListItem<?> item_){
		Nav.go(getActivity(), SettingsFiltersFragment.class, makeFragmentArgs());
	}

	private void onNotificationsClick(ListItem<?> item_){
		Nav.go(getActivity(), SettingsNotificationsFragment.class, makeFragmentArgs());
	}

	private void onInstanceClick(ListItem<?> item_){
		Nav.go(getActivity(), SettingsInstanceFragment.class, makeFragmentArgs());
	}

	private void onAboutClick(ListItem<?> item_){
		Nav.go(getActivity(), SettingsAboutAppFragment.class, makeFragmentArgs());
	}

	private void onManageAccountsClick(ListItem<?> item){
		new AccountSwitcherSheet(getActivity(), null).show();
	}

	private void onLogOutClick(ListItem<?> item_){
		AccountSession session=AccountSessionManager.getInstance().getAccount(accountID);
		new M3AlertDialogBuilder(getActivity())
				.setTitle(R.string.log_out)
				.setMessage(getString(R.string.confirm_log_out, session.getFullUsername()))
				.setPositiveButton(R.string.log_out, (dialog, which)->account.logOut(getActivity(), ()->{
					loggedOut=true;
					((MainActivity)getActivity()).restartActivity();
				}))
				.setNegativeButton(R.string.cancel, null)
				.show();
	}
}
