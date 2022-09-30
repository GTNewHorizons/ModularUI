package com.gtnewhorizons.modularui.common.widget;

import com.gtnewhorizons.modularui.api.math.Size;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * Util widget for syncing pre-defined values.
 * Replacement of updateProgressBar workaround!
 * Does not draw anything.
 */
@SuppressWarnings("unused")
public class FakeSyncWidget<T> extends SyncedWidget {

    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private final BiConsumer<PacketBuffer, T> writeToBuffer;
    private final Function<PacketBuffer, T> readFromBuffer;

    private T lastValue;

    private Consumer<T> onClientUpdate;

    private static final int MAX_PACKET_LENGTH = Short.MAX_VALUE;

    public FakeSyncWidget(
            Supplier<T> getter,
            Consumer<T> setter,
            BiConsumer<PacketBuffer, T> writeToBuffer,
            Function<PacketBuffer, T> readFromBuffer) {
        this.getter = getter;
        this.setter = setter;
        this.writeToBuffer = writeToBuffer;
        this.readFromBuffer = readFromBuffer;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == 0) {
            T newValue = readFromBuffer.apply(buf);
            setter.accept(newValue);
            if (onClientUpdate != null) {
                onClientUpdate.accept(newValue);
            }
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {}

    @Override
    public void detectAndSendChanges(boolean init) {
        T newValue = getter.get();
        if (init || !Objects.equals(lastValue, newValue)) {
            lastValue = newValue;
            syncToClient(0, buffer -> writeToBuffer.accept(buffer, newValue));
        }
    }

    @Override
    public void drawBackground(float partialTicks) {}

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return new Size(0, 0);
    }

    public static <E> void writeListToBuffer(
            PacketBuffer buffer, List<E> toWrite, BiConsumer<PacketBuffer, E> writeElementToBuffer) {
        buffer.writeVarIntToBuffer(toWrite.size());
        for (E element : toWrite) {
            writeElementToBuffer.accept(buffer, element);
        }
    }

    public static <E> List<E> readListFromBuffer(PacketBuffer buffer, Function<PacketBuffer, E> readElementFromBuffer) {
        int size = buffer.readVarIntFromBuffer();
        List<E> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(readElementFromBuffer.apply(buffer));
        }
        return list;
    }

    public FakeSyncWidget<T> onClientUpdate(Consumer<T> onClientUpdate) {
        this.onClientUpdate = onClientUpdate;
        return this;
    }

    public static class BooleanSyncer extends FakeSyncWidget<Boolean> {
        public BooleanSyncer(Supplier<Boolean> getter, Consumer<Boolean> setter) {
            super(getter, setter, PacketBuffer::writeBoolean, PacketBuffer::readBoolean);
        }
    }

    public static class ByteSyncer extends FakeSyncWidget<Byte> {
        public ByteSyncer(Supplier<Byte> getter, Consumer<Byte> setter) {
            super(getter, setter, PacketBuffer::writeByte, PacketBuffer::readByte);
        }
    }

    public static class ShortSyncer extends FakeSyncWidget<Short> {
        public ShortSyncer(Supplier<Short> getter, Consumer<Short> setter) {
            super(getter, setter, PacketBuffer::writeShort, PacketBuffer::readShort);
        }
    }

    public static class IntegerSyncer extends FakeSyncWidget<Integer> {
        public IntegerSyncer(Supplier<Integer> getter, Consumer<Integer> setter) {
            super(getter, setter, PacketBuffer::writeVarIntToBuffer, PacketBuffer::readVarIntFromBuffer);
        }
    }

    public static class LongSyncer extends FakeSyncWidget<Long> {
        public LongSyncer(Supplier<Long> getter, Consumer<Long> setter) {
            super(getter, setter, PacketBuffer::writeLong, PacketBuffer::readLong);
        }
    }

    public static class FloatSyncer extends FakeSyncWidget<Float> {
        public FloatSyncer(Supplier<Float> getter, Consumer<Float> setter) {
            super(getter, setter, PacketBuffer::writeFloat, PacketBuffer::readFloat);
        }
    }

    public static class StringSyncer extends FakeSyncWidget<String> {
        public StringSyncer(Supplier<String> getter, Consumer<String> setter) {
            super(
                    getter,
                    setter,
                    (buffer, val) -> {
                        try {
                            buffer.writeStringToBuffer(val);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    },
                    buffer -> {
                        try {
                            return buffer.readStringFromBuffer(MAX_PACKET_LENGTH);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return "";
                        }
                    });
        }
    }

    public static class ItemStackSyncer extends FakeSyncWidget<ItemStack> {
        public ItemStackSyncer(Supplier<ItemStack> getter, Consumer<ItemStack> setter) {
            super(
                    getter,
                    setter,
                    (buffer, stack) -> {
                        try {
                            buffer.writeItemStackToBuffer(stack);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    },
                    buffer -> {
                        try {
                            return buffer.readItemStackFromBuffer();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    });
        }
    }

    public static class ListSyncer<U> extends FakeSyncWidget<List<U>> {
        public ListSyncer(
                Supplier<List<U>> getter,
                Consumer<List<U>> setter,
                BiConsumer<PacketBuffer, U> writeElementToBuffer,
                Function<PacketBuffer, U> readElementFromBuffer) {
            super(
                    getter,
                    setter,
                    (buffer, val) -> FakeSyncWidget.writeListToBuffer(buffer, val, writeElementToBuffer),
                    buffer -> FakeSyncWidget.readListFromBuffer(buffer, readElementFromBuffer));
        }
    }
}
