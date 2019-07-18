import time as t, math from 'std.tf';
import assert, expect, expect_error, to, describe, it, subject, before, after from "std/spec";

alias t.of_unix_timestamp as of_unix_timestamp;

library spec {
  spec:
    describe("time.of_unix_timestamp", [

      it("of_default", () ->
        expect(of_unix_timestamp(), to.be_nil())
      ),

      it("of_nil", () ->
        expect(of_unix_timestamp(nil), to.be_nil())
      ),

      it("of_epoch", () ->
        expect(of_unix_timestamp(0), to.be(t.epoch))
      ),

      it("of_1496933995", () ->
        expect(of_unix_timestamp(1496933995), to.be(2017-06-08T14:59:55Z@UTC))
      ),

      it("of_neg_1496933995", () ->
        expect(of_unix_timestamp(-1496933995), to.be(1922-07-26T09:00:05Z@UTC))
      ),

      it("of_overflow", () ->
        expect_error(
          () -> of_unix_timestamp(math.max_long),
          to.have_code("ILLEGAL_ARGUMENT")
        )
      ),

      it("of_underflow", () ->
        expect_error(
          () -> of_unix_timestamp(math.min_long),
          to.have_code("ILLEGAL_ARGUMENT")
        )
      ),
  ]);
}