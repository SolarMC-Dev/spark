/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.lucko.spark.common.sampler;

import me.lucko.spark.common.command.sender.CommandSender;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.sampler.node.MergeMode;
import me.lucko.spark.common.sampler.node.ThreadNode;
import me.lucko.spark.common.util.ClassSourceLookup;
import me.lucko.spark.proto.SparkProtos.SamplerData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPOutputStream;

/**
 * Abstract superinterface for all sampler implementations.
 */
public interface Sampler {

    /**
     * Starts the sampler.
     */
    void start();

    /**
     * Stops the sampler.
     */
    void stop();

    /**
     * Gets the time when the sampler started (unix timestamp in millis)
     *
     * @return the start time
     */
    long getStartTime();

    /**
     * Gets the time when the sampler should automatically stop (unix timestamp in millis)
     *
     * @return the end time, or -1 if undefined
     */
    long getEndTime();

    /**
     * Gets a future to encapsulate the completion of the sampler
     *
     * @return a future
     */
    CompletableFuture<? extends Sampler> getFuture();

    // Methods used to export the sampler data to the web viewer.
    SamplerData toProto(ExportProps props);

    default byte[] formCompressedDataPayload(ExportProps props) {
        SamplerData proto = toProto(props);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try (OutputStream out = new GZIPOutputStream(byteOut)) {
            proto.writeTo(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteOut.toByteArray();
    }

    class ExportProps {
        public final PlatformInfo platformInfo;
        public final CommandSender creator;
        public final Comparator<? super Map.Entry<String, ThreadNode>> outputOrder;
        public final String comment;
        public final MergeMode mergeMode;
        public final ClassSourceLookup classSourceLookup;

        public ExportProps(PlatformInfo platformInfo, CommandSender creator, Comparator<? super Map.Entry<String, ThreadNode>> outputOrder, String comment, MergeMode mergeMode, ClassSourceLookup classSourceLookup) {
            this.platformInfo = platformInfo;
            this.creator = creator;
            this.outputOrder = outputOrder;
            this.comment = comment;
            this.mergeMode = mergeMode;
            this.classSourceLookup = classSourceLookup;
        }
    }

}
