package ascelion.microprofile.config.eval;

import java.util.Deque;
import java.util.LinkedList;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class Replacer {
	private final Expression exp;
	private final Deque<Buffer> vars = new LinkedList<>();

	private void pushName(Buffer var) {
		if (this.vars.contains(var)) {
			final String m = format("Recursive definition for ${%s}: %s", var,
					this.vars.stream().map(Buffer::toString).collect(joining(" -> ")));

			throw new IllegalStateException(m);
		}

		this.vars.addLast(var);
	}

	private void popName() {
		this.vars.pollLast();
	}

	Buffer replace(Buffer buf) {
		int ofs1 = buf.offset;

		while (ofs1 < buf.offset + buf.count) {
			int next = buf.match(this.exp.varPrefix, ofs1);

			if (next > ofs1) {
				ofs1 = next;

				continue;
			}

			// prefix matched
			int ofs2 = ofs1 + this.exp.varPrefix.length;
			int open = 1;

			while (ofs2 < buf.offset + buf.count) {
				next = buf.match(this.exp.varPrefix, ofs2);

				if (next == ofs2) {
					// matched nested prefix
					open++;

					ofs2 += this.exp.varPrefix.length;

					continue;
				}

				next = buf.match(this.exp.varSuffix, ofs2);

				if (next > ofs2) {
					ofs2 = next;

					continue;
				}
				// matched suffix
				if (--open > 0) {
					ofs2 += this.exp.varSuffix.length;

					continue;
				}

				handleLastSuffix(buf, ofs1, ofs2 - ofs1);

				break;
			}

			if (open > 0) {
				break;
			}
		}

		return buf;
	}

	private void handleLastSuffix(final Buffer buf, final int ofs, final int cnt) {
		final Buffer place = buf.newBuffer(ofs + this.exp.varPrefix.length, cnt - this.exp.varPrefix.length);

		replace(place);

		final int defIx = place.find(this.exp.valueSep, 0);
		final String def;
		Buffer var;

		if (defIx < 0) {
			var = place;
			def = null;
		} else {
			var = place.subBuffer(0, defIx);
			def = place.subBuffer(defIx + this.exp.valueSep.length, place.count - defIx - this.exp.valueSep.length).toString();
		}

		String val = this.exp.lookup.apply(var.toString(), def);

		pushName(var);
		val = replace(new Buffer(trimToEmpty(val))).toString();
		popName();

		buf.replace(ofs, cnt + this.exp.varSuffix.length, val);
	}

}
