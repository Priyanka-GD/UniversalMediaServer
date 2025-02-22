/*
 * This file is part of Universal Media Server, based on PS3 Media Server.
 *
 * This program is a free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; version 2 of the License only.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package net.pms.newgui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.metal.MetalIconFactory;
import net.pms.Messages;
import net.pms.renderers.Renderer;
import net.pms.renderers.devices.players.BasicPlayer;
import net.pms.renderers.devices.players.LogicalPlayer;
import net.pms.renderers.devices.players.PlayerState;
import net.pms.util.UMSUtils;
import org.apache.commons.lang.StringUtils;

public class PlayerControlPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 8972730138916895247L;

	private LogicalPlayer player;
	@SuppressWarnings("unused")
	private AbstractAction add, remove, clear, play, pause, stop, next, prev, forward, rewind, mute, volume, seturi, excl;
	private Button position;
	private JSlider volumeSlider;
	private JTextField uri;
	private JComboBox uris;
	private boolean edited, playing;
	private String lasturi;
	private File pwd;
	private boolean playControl, volumeControl, expanded;
	int sliding;

	private static ImageIcon addIcon, removeIcon, clearIcon, playIcon, pauseIcon, stopIcon, fwdIcon, rewIcon,
		nextIcon, prevIcon, volumeIcon, muteIcon, sliderIcon;

	public PlayerControlPanel(final BasicPlayer player) {
		if (playIcon == null) {
			loadIcons();
		}
		this.player = (LogicalPlayer) player;
		player.connect(this);
		int controls = player.getControls();
		playControl = (controls & Renderer.PLAYCONTROL) != 0;
		volumeControl = (controls & Renderer.VOLUMECONTROL) != 0;
		expanded = true;
		sliding = 0;

		try {
			pwd = new File(player.getState().getUri()).getParentFile();
		} catch (Exception e) {
			pwd = new File("");
		}

		setPreferredSize(new Dimension(530, 70));
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(6, 0, 0, 0);

		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 1.0;
		c.weightx = c.weighty;

		Toolbar ctrl = new Toolbar();
		ctrl.setLayout(new BoxLayout(ctrl, BoxLayout.X_AXIS));
		if (volumeControl) {
			if (playControl) {
				addVolumeControls(ctrl);
			} else {
				ctrl.add(Box.createHorizontalGlue());
				addVolumeControls(ctrl);
				ctrl.add(Box.createHorizontalGlue());
				add(ctrl, c);
			}
		}
		if (playControl) {
			ctrl.add(Box.createHorizontalStrut(volumeControl ? 55 : 140));
			addPlayControls(ctrl);
			ctrl.add(Box.createHorizontalGlue());
			addStatus(ctrl);
			add(ctrl, c);
			c.gridy++;
			Toolbar toolbar = new Toolbar();
			addUriControls(toolbar);
			add(toolbar, c);
		}

		player.alert();

		final ActionListener self = this;
		getEnclosingWindow(this).addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				player.disconnect(self);
			}
		});
	}

	public final void addPlayControls(Container parent) {
		prev = new AbstractAction("", prevIcon) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				player.prev();
			}
		};
		parent.add(new Button(36, prev));
		rewind = new AbstractAction("", rewIcon) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				player.rewind();
			}
		};
		parent.add(new Button(36, rewind));
		play = new AbstractAction("", playIcon) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				setEdited(false);
				player.pressPlay(uri.getText(), null);
			}
		};
		parent.add(new Button(36, play));
		stop = new AbstractAction("", stopIcon) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				player.pressStop();
			}
		};
		parent.add(new Button(36, stop));
		forward = new AbstractAction("", fwdIcon) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				player.forward();
			}
		};
		parent.add(new Button(36, forward));
		next = new AbstractAction("", nextIcon) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				player.next();
			}
		};
		parent.add(new Button(36, next));
	}

	public final void addStatus(final Container parent) {
		position = new Button(new AbstractAction("") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				toggleView(parent);
			}
		});
		parent.add(position);
		position.setHorizontalAlignment(SwingConstants.RIGHT);
		position.setToolTipText(Messages.getString("ShowHideDetails"));
	}

	public final void addVolumeControls(Container parent) {
		UIDefaults defaults = UIManager.getDefaults();
		Object hti = defaults.put("Slider.horizontalThumbIcon", sliderIcon);
		Object tb = defaults.put("Slider.trackBorder", BorderFactory.createEmptyBorder());

		volumeSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		Dimension d = new Dimension(80, 20);
		volumeSlider.setPreferredSize(d);
		volumeSlider.setSize(d);
		volumeSlider.setMaximumSize(d);
		volumeSlider.addChangeListener((ChangeEvent e) -> {
			// Fire only when the slider is in motion, i.e. not during external updates
			if (((JSlider) e.getSource()).getValueIsAdjusting()) {
				player.setVolume(volumeSlider.getValue());
				// For smoothness ignore external volume data until
				// the 3rd update after sliding has finished
				sliding = 3;
			}
		});
		volumeSlider.setFocusable(false);
//		volumeSlider.setPaintLabels(true);
//		volumeSlider.setLabelTable(new Hashtable<Integer, JLabel>() {{
//			put(0, new JLabel("<html><b>-</b></html>"));
//			put(100, new JLabel("<html><b>+</b></html>"));
//		}});
//		volumeSlider.setAlignmentX(0.25f);
		parent.add(volumeSlider);
		defaults.put("Slider.horizontalThumbIcon", hti);
		defaults.put("Slider.trackBorder", tb);

		mute = new AbstractAction("", volumeIcon) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				player.mute();
			}
		};
		Button muteButton = new Button(mute);
		parent.add(muteButton);
	}

	public final void addUriControls(Container parent) {
		uris = new JComboBox(player.getPlaylist());
		uris.setMaximumRowCount(20);
		uris.setEditable(true);
		// limit width to available space
		uris.setPrototypeDisplayValue("");
		uri = (JTextField) uris.getEditor().getEditorComponent();
		uri.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
				SwingUtilities.invokeLater(() -> uri.select(0, 0));
			}
		});
		uri.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				setEdited(true);
				if (!playing) {
					play.setEnabled(!StringUtils.isBlank(uri.getText()));
				}
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}
		});

		parent.add(uris);
		add = new AbstractAction("", addIcon) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				setEdited(false);
				player.add(-1, uri.getText(), null, null, true);
			}
		};
		Button a = new Button(add);
		a.setToolTipText(Messages.getString("AddToPlaylist"));
		parent.add(a);
		remove = new AbstractAction("", removeIcon) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				player.remove(uri.getText());
			}
		};
		Button r = new Button(remove);
		r.setToolTipText(Messages.getString("RemoveFromPlaylist"));
		parent.add(r);
		clear = new AbstractAction("", clearIcon) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				player.clear();
			}
		};
		Button c = new Button(clear);
		c.setToolTipText(Messages.getString("ClearPlaylist"));
		parent.add(c);
		parent.add(new Button(new AbstractAction("", MetalIconFactory.getTreeFolderIcon()) {
			private static final long serialVersionUID = -2826057503405341316L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(pwd);
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					uri.setText(fc.getSelectedFile().getPath());
					setEdited(true);
				}
				pwd = fc.getCurrentDirectory();
			}
		}));
	}

	public static Window getEnclosingWindow(Component c) {
		return c == null ? JOptionPane.getRootFrame() :
			c instanceof Window ? (Window) c : getEnclosingWindow(c.getParent());
	}

	public BasicPlayer getPlayer() {
		return player;
	}

	public void setEdited(boolean b) {
		edited = b;
		updatePlaylist();
	}

	public void updatePlaylist() {
		boolean empty = uris.getModel().getSize() == 0;
		add.setEnabled((edited || empty) && StringUtils.isNotBlank(uri.getText()));
		remove.setEnabled(!empty);
		clear.setEnabled(!empty);
		boolean more = uris.getModel().getSize() > 1;
		next.setEnabled(more);
		prev.setEnabled(more);
		edited = false;
	}

	public void refreshPlayerState(PlayerState state) {
		if (playControl) {
			playing = !state.isStopped();
			// update playback status
			play.putValue(Action.SMALL_ICON, state.isPlaying() ? pauseIcon : playIcon);
			stop.setEnabled(playing);
			forward.setEnabled(playing);
			rewind.setEnabled(playing);
			// update position
			position.setText(UMSUtils.playedDurationStr(state.getPosition(), state.getDuration()));
			// update uris only if meaningfully new
			boolean isNew = !StringUtils.isBlank(state.getUri()) && !state.getUri().equals(lasturi);
			lasturi = state.getUri();
			if (isNew) {
				if (edited) {
					player.add(-1, uri.getText(), null, null, false);
					setEdited(false);
				}
				uri.setText(state.getName());
			}
			play.setEnabled(playing || !StringUtils.isBlank(uri.getText()));
			updatePlaylist();
		}
		if (volumeControl) {
			// update rendering status
			mute.putValue(Action.SMALL_ICON, state.isMuted() ? muteIcon : volumeIcon);
			volumeSlider.setEnabled(!state.isMuted());
			// ignore volume during slider motion
			if (--sliding < 0) {
				volumeSlider.setValue(state.getVolume());
			}
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		SwingUtilities.invokeLater(() -> {
			refreshPlayerState(((BasicPlayer) e.getSource()).getState());
		});
	}

	private static void loadIcons() {
		addIcon    = loadIcon("/resources/images/player/add16.png");
		removeIcon = loadIcon("/resources/images/player/remove16.png");
		clearIcon  = loadIcon("/resources/images/player/clear16.png");
		playIcon   = loadIcon("/resources/images/player/play16.png");
		pauseIcon  = loadIcon("/resources/images/player/pause16.png");
		stopIcon   = loadIcon("/resources/images/player/stop16.png");
		fwdIcon    = loadIcon("/resources/images/player/fwd16.png");
		rewIcon    = loadIcon("/resources/images/player/rew16.png");
		nextIcon   = loadIcon("/resources/images/player/next16.png");
		prevIcon   = loadIcon("/resources/images/player/prev16.png");
		volumeIcon = loadIcon("/resources/images/player/vol16.png");
		muteIcon   = loadIcon("/resources/images/player/mute16.png");
		sliderIcon = loadIcon("/resources/images/player/bar16.png");
	}

	private static ImageIcon loadIcon(String path) {
		URL url = PlayerControlPanel.class.getResource(path);
		if (url != null) {
			return new ImageIcon(url);
		}
		throw new RuntimeException("icon not found: " + path);
	}

	public void toggleView(Component child) {
		Component anchor = child.getParent();
		anchor.setPreferredSize(anchor.getSize());
		// Toggle sibling visibility
		expanded = !expanded;
		for (Component c : anchor.getParent().getComponents()) {
			if (c != anchor) {
				c.setVisible(expanded);
			}
		}
		// Redraw without moving the anchor (if possible)
		int y = (int) anchor.getLocation().getY();
		JFrame top = (JFrame) SwingUtilities.getWindowAncestor(this);
		top.setVisible(false);
		top.pack();
		Point p = top.getLocation();
		top.setLocation((int) p.getX(), y - (int) anchor.getLocation().getY() + (int) p.getY());
		top.setVisible(true);
	}

	static class Button extends JButton {
		private static final long serialVersionUID = 8649059925768844933L;
		public Button(Action a) {
			this(0, a);
		}
		public Button(int width, Action a) {
			super(a);
			if (width > 0) {
				setPreferredSize(new Dimension(width, 30));
			}
			setFocusable(false);
		}
	}

	static class Toolbar extends JToolBar {
		private static final long serialVersionUID = -657958964967514184L;

		public Toolbar() {
			super(SwingConstants.HORIZONTAL);
			setFloatable(false);
			setRollover(true);
			setOpaque(false);
			setBorderPainted(false);
		}
	}
}
