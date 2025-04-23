---
title : "Mermaid"
mermaid: true
hidden: true
description : "Generation of diagram and flowchart from text in a similar manner as markdown"
---

[Mermaid](https://mermaidjs.github.io/) is a library helping you to generate diagram and flowcharts from text, in a similar manner as Markdown.

In order to have the latest version of mermaid, a layout has been added, to use it, add `mermaid: true` in the header of your markdown file

Now, you can insert your mermaid code in the `mermaid` shortcode and that's it.

*Note:*
- *By default, the 'forest' theme and 'center' alignment are used, you can just override alignment.*

## Flowchart example

    {{</*mermaid align="right"*/>}}
    graph LR;
        A[Hard edge] -->|Link text| B(Round edge)
        B --> C{Decision}
        C -->|One| D[Result one]
        C -->|Two| E[Result two]
    {{</* /mermaid */>}}

renders as

{{<mermaid align="right">}}
graph LR;
    A[Hard edge] -->|Link text| B(Round edge)
    B --> C{Decision}
    C -->|One| D[Result one]
    C -->|Two| E[Result two]
{{</mermaid>}}

## Sequence example

    {{</* mermaid */>}}
    sequenceDiagram
        participant Alice
        participant Bob
        Alice->>John: Hello John, how are you?
        loop Healthcheck
            John->John: Fight against hypochondria
        end
        Note right of John: Rational thoughts <br/>prevail...
        John-->Alice: Great!
        John->Bob: How about you?
        Bob-->John: Jolly good!
    {{</* /mermaid */>}}

renders as

{{<mermaid>}}
sequenceDiagram
    participant Alice
    participant Bob
    Alice->>John: Hello John, how are you?
    loop Healthcheck
        John->John: Fight against hypochondria
    end
    Note right of John: Rational thoughts <br/>prevail...
    John-->Alice: Great!
    John->Bob: How about you?
    Bob-->John: Jolly good!
{{</mermaid>}}

## GANTT Example

    {{</* mermaid */>}}
    gantt
            dateFormat  YYYY-MM-DD
            title Adding GANTT diagram functionality to mermaid
            section A section
            Completed task            :done,    des1, 2014-01-06,2014-01-08
            Active task               :active,  des2, 2014-01-09, 3d
            Future task               :         des3, after des2, 5d
            Future task2               :         des4, after des3, 5d
            section Critical tasks
            Completed task in the critical line :crit, done, 2014-01-06,24h
            Implement parser and jison          :crit, done, after des1, 2d
            Create tests for parser             :crit, active, 3d
            Future task in critical line        :crit, 5d
            Create tests for renderer           :2d
            Add to mermaid                      :1d
    {{</* /mermaid */>}}


renders as

{{<mermaid>}}
gantt
        dateFormat  YYYY-MM-DD
        title Adding GANTT diagram functionality to mermaid
        section A section
        Completed task            :done,    des1, 2014-01-06,2014-01-08
        Active task               :active,  des2, 2014-01-09, 3d
        Future task               :         des3, after des2, 5d
        Future task2               :         des4, after des3, 5d
        section Critical tasks
        Completed task in the critical line :crit, done, 2014-01-06,24h
        Implement parser and jison          :crit, done, after des1, 2d
        Create tests for parser             :crit, active, 3d
        Future task in critical line        :crit, 5d
        Create tests for renderer           :2d
        Add to mermaid                      :1d
{{</mermaid>}}


### Class example

    {{</* mermaid */>}}
    classDiagram
      Class01 <|-- AveryLongClass : Cool
      Class03 *-- Class04
      Class05 o-- Class06
      Class07 .. Class08
      Class09 --> C2 : Where am i?
      Class09 --* C3
      Class09 --|> Class07
      Class07 : equals()
      Class07 : Object[] elementData
      Class01 : size()
      Class01 : int chimp
      Class01 : int gorilla
      Class08 <--> C2: Cool label
    {{</* /mermaid */>}}

renders as


{{<mermaid>}}
classDiagram
  Class01 <|-- AveryLongClass : Cool
  Class03 *-- Class04
  Class05 o-- Class06
  Class07 .. Class08
  Class09 --> C2 : Where am i?
  Class09 --* C3
  Class09 --|> Class07
  Class07 : equals()
  Class07 : Object[] elementData
  Class01 : size()
  Class01 : int chimp
  Class01 : int gorilla
  Class08 <--> C2: Cool label
{{</mermaid>}}


### Git example

    {{</* mermaid */>}}
    gitGraph:
    options
    {
      "nodeSpacing": 150,
      "nodeRadius": 10
    }
    end
        commit
        commit
        branch develop
        checkout develop
        commit
        commit
        checkout main
        merge develop
        commit
        commit
    {{</* /mermaid*/>}}

renders as

{{<mermaid>}}
---
title: Example Git diagram
---
gitGraph
options
{
    "nodeSpacing": 150,
    "nodeRadius": 10
}
end
    commit
    commit
    branch develop
    checkout develop
    commit
    commit
    checkout main
    merge develop
    commit
    commit
{{</mermaid>}}

### State Diagrams

    {{</* mermaid */>}}
    stateDiagram-v2
      open: Open Door
      closed: Closed Door
      locked: Locked Door
      open   --> closed: Close
      closed --> locked: Lock
      locked --> closed: Unlock
      closed --> open: Open
    {{</* /mermaid */>}}

renders as

{{<mermaid>}}
stateDiagram-v2
  open: Open Door
  closed: Closed Door
  locked: Locked Door
  open   --> closed: Close
  closed --> locked: Lock
  locked --> closed: Unlock
  closed --> open: Open
{{</mermaid>}}
