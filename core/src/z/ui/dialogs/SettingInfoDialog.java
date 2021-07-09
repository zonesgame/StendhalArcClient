package z.ui.dialogs;

import arc.Core;
import arc.input.KeyCode;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.type.TileEntity;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.FloatingDialog;

public class SettingInfoDialog extends FloatingDialog {

    public SettingInfoDialog(){
        super("$setting.title");

        addCloseButton();
    }

    public void show(UnlockableContent content, TileEntity tileEntity){
        cont.clear();

        Table table = new Table();
        table.margin(10);
        // zones add
        table.setUserObject(tileEntity);

        content.displayInfo(table);

        ScrollPane pane = new ScrollPane(table);
//        pane.setFadeScrollBars(true);
//        pane.setClamp(true);
        cont.add(pane);

        show();
    }

    @Override
    public void addCloseButton(){
        buttons.defaults().size(210f, 64f);
        buttons.addImageTextButton("$close", Icon.left, this::hide).size(210f, 64f);

        keyDown(key -> {
            if(key == KeyCode.ESCAPE || key == KeyCode.BACK){
                Core.app.post(this::hide);
            }
        });
    }
}
