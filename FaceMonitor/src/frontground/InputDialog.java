package frontground;

import java.awt.BorderLayout;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JPasswordField;
import background.UserInfo;

public class InputDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textName;
	private JLabel lblWarning;

	
	private boolean isOK = false;
	
	public boolean isOK() {
		return isOK;
	}
	
	public String getUserName() {
		return textName.getText();
	}

	/**
	 * Launch the application.
	 */
	
	/**
	 * Create the dialog.
	 */
	public InputDialog() {

		setBounds(100, 100, 327, 176);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		textName = new JTextField();
		textName.setBounds(74, 25, 205, 21);
		contentPanel.add(textName);
		textName.setColumns(10);
		
		JLabel lblName = new JLabel("Name:");
		lblName.setFont(new Font("Dialog", Font.PLAIN, 16));
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);
		lblName.setBounds(10, 27, 54, 15);
		contentPanel.add(lblName);
		

		{
			JButton cancelButton = new JButton("Cancel");
			cancelButton.setBounds(205, 105, 96, 23);
			contentPanel.add(cancelButton);
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					
					setVisible(false);
				}
			});
			getRootPane().setDefaultButton(cancelButton);
			
			
		}
		{
			JButton okButton = new JButton("OK");
			okButton.setBounds(98, 105, 97, 23);
			contentPanel.add(okButton);
			
				okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					pressOK();
					
				}
			});
			getRootPane().setDefaultButton(okButton);
		}
		
		lblWarning = new JLabel("");
		lblWarning.setFont(new Font("Arial", Font.PLAIN, 13));
		lblWarning.setBounds(74, 56, 205, 27);
		contentPanel.add(lblWarning);
	}
	
	protected void pressOK() {
		if(textName.getText().equals(""))
		{
			lblWarning.setText("Name is empty!");
			return;
		}
		isOK = true;
		setVisible(false);
		
	}
	public void clear() {
		isOK = false;
		lblWarning.setText(null);
		textName.setText(null);
		
	}
}


