import javax.swing.*;  //usei alguns sites pra achar alguns comandos e tals, vou deixar os links no finals do codigo em comit
import java.awt.*;    //sendo bem sincero eu me diverti bastante fazendo isso, pesquisei e tentei organizar bonitinho e tals ate vi norma de identação e tudo mais, aprendi bastante nisso gostei
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.sql.*;
import javax.swing.filechooser.FileNameExtensionFilter;

//ate aqui só faz imports e todo import tem que estar no começo, por isso ta aqui

public class ImageUploader {
    //chama assim porque é literalemnte o que ele faz entao mais facil
    private Connection connection;

    public static void main(String[] args) {
        new ImageUploader().createAndShowGUI();
    }

    public ImageUploader() {
        try {
            // faz a conexão com o banco existir
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/personagens_db", "usuario", "senha"); //conecta ao banco de personagens que eu chamei de personagens, olha que criativo
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
         //tela
    private void createAndShowGUI() {
        JFrame frame = new JFrame("adiciona  Imagens");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel titleLabel = new JLabel("nome do personagem:");
        JTextField titleField = new JTextField(20);
        JButton colorButton = new JButton("escolhe a Cor");
        JButton uploadButton = new JButton("carregar Imagem");
        JButton saveButton = new JButton("salvar");
        JButton showButton = new JButton("Imagens salvas");
        JLabel imageLabel = new JLabel();
        File selectedImageFile = null;
        Color selectedColor = Color.BLACK;
        //tudo isso em função de adicionar as coisas relacionadas ao personagem pra aparecer na outra tela

        colorButton.addActionListener(e -> {   // ta colorButton porque por algum motivo  se escrever de qualquer outra maneira faz kabum, pode ser que minha extensão ta pra ingles por isso certas coisas sao definidas somente em ingles ou ta bugada mesmo
            selectedColor = JColorChooser.showDialog(frame, "escolhe uma cor", Color.BLACK);
        }); //pra ser mais customizavel voce pode escolher as cores que voce quer usar em cada personagem

        uploadButton.addActionListener(e -> {   //caso voce queira atualizar
            JFileChooser fileChooser = new JFileChooser(); //escolhe a sua midia vulgo imagem
            fileChooser.setFileFilter(new FileNameExtensionFilter("Imagens", "jpg", "png", "jpeg")); //separa pra não colocar outras coisas e kabum
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) { //aplica condição que se cumprida ta ok
                selectedImageFile = fileChooser.getSelectedFile();
                try {
                    BufferedImage img = ImageIO.read(selectedImageFile);
                    imageLabel.setIcon(new ImageIcon(img));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }  //ate aqui tudo gira em torno de fazer a imagem funcionar, achei esses comandos em um forum e deu bom real
            }
        });

        saveButton.addActionListener(e -> { //finalmente consegui usar um listener certo
            try {
                String title = titleField.getText();
                String sql = "INSERT INTO imagens (nome do personagem, cor, imagem) VALUES (?, ?, ?)"; //aqui você ta colocando as informações de texto como o tirulo que é o nome do personagem cor que eu liberei e une com a imagem ja que adiciona e fodasse
                PreparedStatement stmt = connection.prepareStatement(sql); //olha la o sql vindo
                stmt.setString(1, title);
                stmt.setString(2, String.format("#%02x%02x%02x", selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue()));

                // vai salvar as imagens no banco, ou seja aqui que executa as coisas relacionadas a imagem, tem que vir depois se nao ficar feio e a identação que manda
                stmt.setBlob(3, new FileInputStream(selectedImageFile));
                stmt.executeUpdate(); //faz o update, vulgo atualiza
                stmt.close(); //fecha porque deu certo entao tem que fechar se nao nao salva ne 
                JOptionPane.showMessageDialog(frame, "Imagem salva"); //mensagem pro songomongo saber que deu bom
            } catch (Exception ex) {  //cria um caso erro
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Erro, não pode ser salvo"); //se colocar algo que nao deveria da nisso ai exibe essa mensagem
            }
        });

        showButton.addActionListener(e -> {
            new ImageDisplayFrame(connection).setVisible(true);
        });

        frame.add(titleLabel); //frame.add adiciona algo visual digamos assim, botão por exemplo que ta ate aqui
        frame.add(titleField);
        frame.add(colorButton);
        frame.add(uploadButton);
        frame.add(saveButton);
        frame.add(showButton);
        frame.add(imageLabel);
        frame.setSize(400, 400); // define tamanho
        frame.setVisible(true); //se voce quer ter interface ela tem que ser visivel né ou seja true=existe e voce ve
         //os frames estão todos juntos por organização e porque deixar no final é o certo pra funcionar gg porque tem coisa que conecta e se ta pracima é mo role
    }
}

class ImageDisplayFrame extends JFrame { //olha la  que bunitu, segunda tela
    private Connection connection;

    public ImageDisplayFrame(Connection connection) { //vem ai a conexão com o que foi salvo pra exibir porque se quer ver ne
        this.connection = connection;
        setTitle("Imagens Salvas"); //as imagens que voce guardou pra ter algo né, entao aqui retorna elas
        setSize(600, 400); //tudo tem um  padrão pra nao ficar feio
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout()); //bordinha good

        JTextArea textArea = new JTextArea(); //zona do texto né
        JScrollPane scrollPane = new JScrollPane(textArea); //scroll.
        add(scrollPane, BorderLayout.CENTER); //ficar numa posição que a gente queira ne 

        try {
            String sql = "SELECT nome do personagem, cor, imagem FROM imagens"; //aqui que la magia é invocada
            PreparedStatement stmt = connection.prepareStatement(sql);  //sql ta na linha?
            ResultSet rs = stmt.executeQuery();   //executa o querry
            while (rs.next()) {
                String title = rs.getString("nome do personagem"); //aqui temos o nome do personagem
                String color = rs.getString("cor");  //aqui jaz a cor
                Blob blob = rs.getBlob("imagem"); //a imagem meio obvio ne?
                byte[] imageBytes = blob.getBytes(1, (int) blob.length());
                ImageIcon imageIcon = new ImageIcon(imageBytes); //nao funcio9na sem 

                // todo texto tem um tamanho
                textArea.append(title + "\n");
                textArea.append("Cor: " + color + "\n");
                textArea.append("\n");
            }
            stmt.close(); //fechouuuuu eeeeee
        } catch (Exception e) {  //exeção 
            e.printStackTrace();
        }
    }
} //meio que aqui terminou ne


//https://www.guj.com.br/t/usando-cores/61477
//https://pt.stackoverflow.com/questions/266169/como-alterar-a-cor-de-um-bot%C3%A3o
//https://pt.stackoverflow.com/questions/136304/como-fa%c3%a7o-para-alterar-o-incremento-de-um-bot%c3%a3o-atraves-de-outro-bot%c3%a3o?rq=1
//https://www.dio.me/articles/boas-praticas-em-java-e-a-sua-importancia   esse foi legal
//https://cursos.alura.com.br/forum/topico-melhor-pratica-de-identacao-do-codigo-java-163032
//https://www.guj.com.br/t/indentacao/90431
//https://www.devmedia.com.br/java-crie-uma-conexao-com-banco-de-dados/5698 tem varias formas então montei essa ai que deu bom
//https://www.youtube.com/watch?v=ntirmRhy6Fw
//https://www.youtube.com/@alexlorenlee  a maior parte do que eu retirei veio daqui, o cara explica muito bem, recomendo
// so coloquei pra sitar algumas das pesquisas que fiz pra montar esse projeto, teve mais coisa porque a curiosidade bateu e tals mas fiz bem simples pra entregar o pedido mesmo e pra me desafiar um pouco a isso tambem