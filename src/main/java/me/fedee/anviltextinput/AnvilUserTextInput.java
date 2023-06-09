package me.fedee.anviltextinput;

import me.TechsCode.UltraCustomizer.UltraCustomizer;
import me.TechsCode.UltraCustomizer.base.item.XMaterial;
import me.TechsCode.UltraCustomizer.scriptSystem.objects.*;
import me.TechsCode.UltraCustomizer.scriptSystem.objects.datatypes.DataType;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

public class AnvilUserTextInput extends Element {

    private final UltraCustomizer plugin;

    public AnvilUserTextInput(UltraCustomizer plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    public String getName() {
        return "Anvil User Text Input";
    }

    public String getInternalName() {
        return "anvil-user-input-text";
    }

    public boolean isHidingIfNotCompatible() {
        return false;
    }

    public XMaterial getMaterial() {
        return XMaterial.WRITABLE_BOOK;
    }

    public String[] getDescription() {
        return new String[] {
            "Will prompt the player",
            "to input text into an anvil.",
        };
    }

    public Argument[] getArguments(ElementInfo elementInfo) {
        return new Argument[] {
                new Argument("player", "Player", DataType.PLAYER, elementInfo),
                new Argument("title", "Title (1.14+)", DataType.STRING, elementInfo),
                new Argument("text", "Text", DataType.STRING, elementInfo),
                new Argument("item-left", "Item Left", DataType.ITEM, elementInfo),

        };
    }


    public OutcomingVariable[] getOutcomingVariables(ElementInfo elementInfo) {
        return new OutcomingVariable[] {
                new OutcomingVariable("input", "Input", DataType.STRING, elementInfo)
        };
    }

    public Child[] getConnectors(final ElementInfo elementInfo) {
        return new Child[] { new Child(elementInfo, "entered") {
            public String getName() {
                return "Text";
            }

            public String[] getDescription() {
                return new String[] { "Text was entered." };
            }

            public XMaterial getIcon() {
                return XMaterial.LIME_STAINED_GLASS_PANE;
            }
        }, new Child(elementInfo, "closed") {
            public String getName() {
                return "Closed";
            }

            public String[] getDescription() {
                return new String[] { "Anvil was closed, input was cancelled." };
            }

            public XMaterial getIcon() {
                return XMaterial.RED_STAINED_GLASS_PANE;
            }
        } };
    }

    @Override
    public void run(ElementInfo elementInfo, ScriptInstance instance) {

        Player player = (Player) getArguments(elementInfo)[0].getValue(instance);
        String title = (String) getArguments(elementInfo)[1].getValue(instance);
        String text = (String) getArguments(elementInfo)[2].getValue(instance);
        ItemStack itemLeft = (ItemStack) getArguments(elementInfo)[3].getValue(instance);

        if (player != null && player.isOnline()) {
            AtomicBoolean clicked = new AtomicBoolean(false);
            new AnvilGUI.Builder()
                    .onClick((slot, stateSnapshot) -> {
                        if (slot != AnvilGUI.Slot.OUTPUT) {
                            return Collections.emptyList();
                        } else {
                        getOutcomingVariables(elementInfo)[0].register(instance, new DataRequester() {
                            @Override
                            public Object request() {
                                return stateSnapshot.getText();
                                }
                            });
                            clicked.set(true);
                            getConnectors(elementInfo)[0].run(instance);

                            return Collections.singletonList(AnvilGUI.ResponseAction.close());
                        }
                    })
                    .onClose(stateSnapshot -> {
                        if (clicked.get()){
                            return;
                        }
                        getOutcomingVariables(elementInfo)[0].register(instance, new DataRequester() {
                            @Override
                            public Object request() {
                                return stateSnapshot.getText();
                                }
                            });
                        getConnectors(elementInfo)[1].run(instance);
                    })
                    .title(title)
                    .text(text)
                    .itemLeft(itemLeft)
                    .plugin(plugin.getBootstrap())
                    .open(player);
        }
    }
}