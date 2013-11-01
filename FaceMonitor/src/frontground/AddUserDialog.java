package frontground;

import java.awt.BorderLayout;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
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

public class AddUserDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textName;
	private JPasswordField passwordOri;
	private JPasswordField passwordRep;
	private JLabel lblWarning;
	private ArrayList<UserInfo> listUser = null;
	
	private boolean userCreated = false;
	
	public boolean isSuccessful() {
		return userCreated;
	}
	public String getUserName() {
		return textName.getText();
	}
	public String getUserPwd() {
		return String.valueOf(passwordOri.getPassword());
	}

	/**
	 * Launch the application.
	 */
	
	/**
	 * Create the dialog.
	 */
	public AddUserDialog(JFrame frame, ArrayList<UserInfo> list) {
		
		super(frame);
		
		this.listUser = list;
		
		setBounds(100, 100, 450, 282);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		textName = new JTextField();
		textName.setBounds(138, 38, 205, 21);
		contentPanel.add(textName);
		textName.setColumns(10);
		
		JLabel label = new JLabel("Username:");
		label.setFont(new Font("Dialog", Font.PLAIN, 16));
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setBounds(28, 40, 98, 15);
		contentPanel.add(label);
		
		JLabel label_1 = new JLabel("Passwords:");
		label_1.setHorizontalAlignment(SwingConstants.RIGHT);
		label_1.setFont(new Font("Dialog", Font.PLAIN, 16));
		label_1.setBounds(28, 75, 97, 15);
		contentPanel.add(label_1);
		
		JLabel lblAgain = new JLabel("Confirm:");
		lblAgain.setHorizontalAlignment(SwingConstants.RIGHT);
		lblAgain.setFont(new Font("Dialog", Font.PLAIN, 16));
		lblAgain.setBounds(28, 114, 98, 19);
		contentPanel.add(lblAgain);
		
		passwordOri = new JPasswordField();
		passwordOri.setBounds(138, 73, 205, 21);
		contentPanel.add(passwordOri);
		
		passwordRep = new JPasswordField();
		passwordRep.setBounds(138, 114, 205, 21);
		contentPanel.add(passwordRep);
		{
			JButton cancelButton = new JButton("Cancel");
			cancelButton.setBounds(328, 211, 96, 23);
			contentPanel.add(cancelButton);
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					
					dispose();
				}
			});
			getRootPane().setDefaultButton(cancelButton);
			
			
		}
		{
			JButton okButton = new JButton("OK");
			okButton.setBounds(221, 211, 97, 23);
			contentPanel.add(okButton);
			
				okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					
					if(textName.getText().equals(""))
					{
						lblWarning.setText("Username is empty!");
						return;
					}
					else if(String.valueOf(passwordOri.getPassword()).equals(""))
					{
						lblWarning.setText("Password is empty!");
						return;
					}
					else if(!String.valueOf(passwordOri.getPassword()).equals(String.valueOf(passwordRep.getPassword())))
					{
						lblWarning.setText("Incorrect!");
						return;
					}
					
					
					boolean rep = false;
					for(UserInfo user : listUser) {
						
						if(textName.getText().equals(user.getName())) {
							rep = true;
							break;
						}
						
					}
					
					if(rep) {
						lblWarning.setText("Username has already exist!");
						return;
					}
					
					userCreated = true;
					
					
					dispose();
					
				}
			});
			getRootPane().setDefaultButton(okButton);
		}
		
		lblWarning = new JLabel("");
		lblWarning.setBounds(138, 157, 205, 27);
		contentPanel.add(lblWarning);
	}
}
