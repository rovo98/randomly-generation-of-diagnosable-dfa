# Random generation of diagnosable DFAs.

Generating diagnosable DFAs randomly to simulate real DESs. Then we use the constructed DFAs to generate running-logs
dataset.

<div align="center" style="display:flex; flex-direction:column;">
    <img src="./images/non-randomly-construction-of-dfas-with-faulty-events.png" />
    <span style='padding-top:0.5em;'>fig 1&nbsp;&nbsp;Single Faulty mode DFA architecture illustration</span>
</div>

<div align='center' style='display:flex; flex-direction:column;'>
    <img src='./images/non-randomly-construction-of-dfas-with-faulty-events_with-extra-normal.png'/>
    <span style="padding-top:0.5em;">Fig 2 Single faulty mode DFA with extra normal state compoent architecture</span>
</div>

## Examples

1. Single faulty mode: one generated running log can only contain one faulty event.
2. Multiply faulty mode: one generated runing log can contain multiply faulty events.

<div style='display:grid; grid-template-rows: 33% 33% 33%; grid-template-columns: 50% 50%;'>
    <div>
        <img src='./images/dfa-example_01_czE0OmZzNDphczk6ZmVzMg==_arch.jpg'/>
    </div>
    <div>
        <img src='./images/dfa-example_02_czE2OmZzNDphczg6ZmVzMg==_arch.png'/>
    </div>
    <div style="padding-top:2em;">
        <img src='./images/dfa-example_03_czE4OmZzNDphczE0OmZlczI=_01_arch.png'/>
    </div>
    <div>
        <img src='./images/dfa-example_04_czE5OmZzNDphczE1OmZlczI=_arch.jpg'/>
    </div>
    <div style="padding-top:5em;">
        <img src='./images/dfa-example_05_czE5OmZzNDphczEzOmZlczI=_arch.jpg'/>
    </div>
    <div>
        <img src='./images/dfa-example_06_czEzOmZzNDphczc6ZmVzMg==_01_arch.png'/>
    </div>
</div>
<div align="center" style="padding-top:0.5em;">
    <span>Fig 3 Generated DFA examples</span>
</div>



### Diagnosability testing

Using the algorithm proposed by Jiang SengBin - ([A polynomial Algorithm for Testing Diagnosability of Discrete-Event System](https://ieeexplore.ieee.org/document/940942) ) to test the diagnosability of the generated DFAs.

e.g.



1. Generated **single faulty**  DFA without extra normal component。

<div align='center'>
    <img src='./images/dfa-example_06_czEzOmZzNDphczc6ZmVzMg==_01_arch.png'/>
</div>

2. Obtains a nondeterministic finite machine of the given DFA.

<div align='center'>
    <img src='./images/dfa-example_06_czEzOmZzNDphczc6ZmVzMg==_02_nd-observer.png'/>
</div>

3. Computes the product composition of the two same nd-observer got above.

<div align='center'>
    <img src='./images/dfa-example_06_czEzOmZzNDphczc6ZmVzMg==_03_composition.png'/>
</div>

4. Checking whether there exists a cycle starting from nodes whose state has different labels.

> This DFA  is not diagnosable。

The following is a example that generated dfa with **single-faulty mode** and **extra normal component**.

1. The architecture of the given constructed dfa.

<div align='center'>
    <img src='./images/dfa-example_08_czE4OmZzNDphczE2OmZlczI=_extra_normal_01_arch.png'/>
</div>

2. Obtains a nondeterministic finite machine of the given dfa.

<div align='center'>
    <img src='./images/dfa-example_08_czE4OmZzNDphczE2OmZlczI=_extra_normal_02_nd_observer.png'/>
</div>

3. Computes the product composition of the two same nondeterministic finite machines got before.

<div align='center'>
    <img src='./images/dfa-example_08_czE4OmZzNDphczE2OmZlczI=_extra_normal_03_composition.png'/>
</div>

> This constructed dfa is diagnosable.

The following is an example that generated dfa with **multi-faulty mode** and **extra normal component**.

1. The architecture of the constructed dfa.

<div align='center'>
    <img src='./images/dfa-example_07_czE3OmZzNDphczE0OmZlczI=_multi-faulty_01_arch.png'/>
</div>

2. Obtains nondeterministic finite machine of the given dfa.

<div align='center'>
    <img src='./images/dfa-example_07_czE3OmZzNDphczE0OmZlczI=_multi-faulty_02_nd-observer.png'/>
</div>

3. Computes the product composition of two same nd-observer got above.

<div align='center'>
    <img src='./images/dfa-example_07_czE3OmZzNDphczE0OmZlczI=_multi-faulty_03_composition.png'/>
</div>

> This dfa is diagnosable.

