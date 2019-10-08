package org.teamapps.app.multi;

import org.teamapps.icons.api.Icon;

import java.util.Objects;

public class ApplicationGroup implements Comparable<ApplicationGroup> {

	public static ApplicationGroup EMPTY_INSTANCE = new ApplicationGroup(null, null);

	private final Icon icon;
	private final String title;
	private int displayRow;

	public ApplicationGroup(Icon icon, String title) {
		this.icon = icon;
		this.title = title;
	}

	public Icon getIcon() {
		return icon;
	}

	public String getTitle() {
		return title;
	}

	public int getDisplayRow() {
		return displayRow;
	}

	public void setDisplayRow(int displayRow) {
		this.displayRow = displayRow;
	}

	@Override
	public int compareTo(ApplicationGroup o) {
		return Integer.compare(displayRow, o.getDisplayRow());
	}
}