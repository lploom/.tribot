package scripts.cowhidekiller;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CowHideKillerGui extends JFrame {

    private boolean getLoot;
    private boolean guiComplete;
    private boolean donePrinceAliQuest;
    private int bankLocation;

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    public boolean getPrinceAliQuest() {
        return donePrinceAliQuest;
    }

    public boolean getLoot() {
        return getLoot;
    }

    public boolean isComplete() {
        return guiComplete;
    }

    public int getBankLocation() {
        return bankLocation;
    }

    public CowHideKillerGui() {
        setTitle("CowHideKiller");
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 310, 250);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblTitle = new JLabel("CowHideKiller[KMScripts]");
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 16));
        lblTitle.setBounds(0, 11, 294, 43);
        contentPane.add(lblTitle);

        final JPanel bankPanel = new JPanel();
        bankPanel.setBounds(10, 91, 284, 78);
        contentPane.add(bankPanel);
        bankPanel.setLayout(null);

        JLabel lblBankMethod = new JLabel("Bank Method");
        lblBankMethod.setBounds(0, 9, 73, 12);
        bankPanel.add(lblBankMethod);
        lblBankMethod.setFont(new Font("Tahoma", Font.PLAIN, 13));

        final JPanel princePanel = new JPanel();
        princePanel.setBounds(10, 32, 264, 35);
        bankPanel.add(princePanel);
        princePanel.setLayout(null);

        final JCheckBox princeAli = new JCheckBox(
                "I have completed Prince Ali Rescue quest");
        princeAli.setBounds(6, 5, 252, 23);
        princeAli.setFont(new Font("Tahoma", Font.PLAIN, 12));
        princePanel.add(princeAli);

        final JComboBox<String> comboBoxBankMethod = new JComboBox<String>();
        comboBoxBankMethod.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (comboBoxBankMethod.getSelectedIndex() == 2) {
                    princePanel.setVisible(true);
                } else {
                    princePanel.setVisible(false);
                }
            }
        });
        comboBoxBankMethod.setBounds(120, 5, 154, 21);
        bankPanel.add(comboBoxBankMethod);
        comboBoxBankMethod.setFont(new Font("Tahoma", Font.PLAIN, 12));
        comboBoxBankMethod.setModel(new DefaultComboBoxModel<String>(
                new String[]{"Lumbridge top floor", "Lumbridge cellar",
                        "Al-kharid"}));
        comboBoxBankMethod.setSelectedIndex(0);

        final JCheckBox checkBoxGetLoot = new JCheckBox("Loot And Bank Cowhides");
        checkBoxGetLoot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (checkBoxGetLoot.isSelected()) {
                    bankPanel.setVisible(true);
                } else {
                    bankPanel.setVisible(false);

                }
            }
        });
        checkBoxGetLoot.setFont(new Font("Tahoma", Font.PLAIN, 12));
        checkBoxGetLoot.setBounds(126, 61, 168, 23);
        contentPane.add(checkBoxGetLoot);
        if (checkBoxGetLoot.isSelected()) {
            bankPanel.setVisible(true);
        } else {
            bankPanel.setVisible(false);
        }

        JLabel lblNewLabel = new JLabel("Loot hides?");
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        lblNewLabel.setBounds(10, 65, 92, 14);
        contentPane.add(lblNewLabel);

        JButton btnStart = new JButton("START");
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getLoot = checkBoxGetLoot.isSelected();
                if (bankPanel.isVisible()) {
                    bankLocation = comboBoxBankMethod.getSelectedIndex();
                } else {
                    bankLocation = -1;
                }
                if (princePanel.isVisible()) {
                    donePrinceAliQuest = princeAli.isSelected();
                } else {
                    donePrinceAliQuest = false;
                }

                if (bankLocation == -1 && getLoot) {
                    System.out.println("No bank location selected!");
                    return;
                }
                guiComplete = true;
            }
        });
        btnStart.setBounds(10, 180, 284, 30);
        contentPane.add(btnStart);
    }
}