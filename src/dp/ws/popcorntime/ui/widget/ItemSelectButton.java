package dp.ws.popcorntime.ui.widget;

import dp.ws.popcorntime.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class ItemSelectButton extends Button implements OnClickListener {

	public interface OnItemSelectedListener {

		public void onItemSelected(int position);

	}

	private class ItemDialog extends DialogFragment implements
			android.content.DialogInterface.OnClickListener {

		private final String DEFAULT_TITLE = "Select Item";

		public ItemDialog() {

		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setCancelable(true);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			if (ItemSelectButton.this.prompt != null
					&& ItemSelectButton.this.prompt.length() > 0) {
				builder.setTitle(ItemSelectButton.this.prompt);
			} else {
				builder.setTitle(DEFAULT_TITLE);
			}
			builder.setAdapter(adapter, ItemDialog.this);
			builder.setNeutralButton(R.string.cancel, null);

			AlertDialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(true);
			final ListView list = dialog.getListView();
			if (list != null) {
				list.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
				list.post(new Runnable() {

					@Override
					public void run() {
						list.setSelection(ItemSelectButton.this.position);
					}

				});
			}

			return dialog;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			setSelection(which);
		}

	}

	private FragmentManager manager;
	private ArrayAdapter<String> adapter;
	private String prompt;
	private ItemDialog dialog;
	private OnItemSelectedListener listener;

	private int position;

	public ItemSelectButton(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		constructor();
	}

	public ItemSelectButton(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		constructor();
	}

	public ItemSelectButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		constructor();
	}

	public ItemSelectButton(Context context) {
		super(context);
		constructor();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (dialog != null && dialog.isAdded()) {
			dialog.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		showDialog();
	}

	public void init(FragmentManager manager, ArrayAdapter<String> adapter) {
		this.manager = manager;
		this.adapter = adapter;
	}

	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		this.listener = listener;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public void showSelectedItem(int position) {
		if (adapter != null && adapter.getCount() > position) {
			this.position = position;
			setText(adapter.getItem(position));
		}
	}

	public void setSelection(int position) {
		showSelectedItem(position);

		if (listener != null) {
			listener.onItemSelected(position);
		}
	}

	private void constructor() {
		this.position = 0;
		this.setOnClickListener(ItemSelectButton.this);
	}

	private void showDialog() {
		if (manager == null) {
			return;
		}

		if (dialog == null) {
			dialog = new ItemDialog();
		}

		if (!dialog.isAdded()) {
			dialog.show(manager,
					"item_select_dialog_" + ItemSelectButton.this.hashCode());
		}
	}

}